use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
use base64::{engine::general_purpose, Engine as _};
use flate2::read::{GzDecoder, GzEncoder};
use flate2::Compression;
use sha2::{Digest, Sha256};
use std::io::Read;

#[derive(Deserialize)]
#[serde(tag = "op")]
pub enum Request {
    #[serde(rename = "compress")]
    Compress { data_b64: String, algo: String },
    #[serde(rename = "decompress")]
    Decompress { data_b64: String, algo: String },
    #[serde(rename = "hash")]
    Hash { data_b64: String, algo: String },
}

#[derive(Serialize)]
pub struct Response {
    pub ok: bool,
    pub result: Value,
    pub mode: String,
}

pub fn process_request(input: &str) -> String {
    match serde_json::from_str::<Request>(input) {
        Ok(Request::Compress { data_b64, algo }) => {
            let data = general_purpose::STANDARD.decode(data_b64).unwrap_or_default();
            let (out, a) = match algo.as_str() {
                "gzip" => {
                    let mut enc = GzEncoder::new(&data[..], Compression::default());
                    let mut buf = Vec::new();
                    let _ = enc.read_to_end(&mut buf);
                    (buf, "gzip")
                }
                _ => (data.clone(), "none"),
            };
            let out_b64 = general_purpose::STANDARD.encode(&out);
            serde_json::to_string(&Response {
                ok: true,
                result: json!({
                    "size_before": data.len(),
                    "size_after": out.len(),
                    "algo": a,
                    "data_b64": out_b64
                }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::Decompress { data_b64, algo }) => {
            let data = general_purpose::STANDARD.decode(data_b64).unwrap_or_default();
            let out = match algo.as_str() {
                "gzip" => {
                    let mut dec = GzDecoder::new(&data[..]);
                    let mut buf = Vec::new();
                    let _ = dec.read_to_end(&mut buf);
                    buf
                }
                _ => data.clone(),
            };
            let out_b64 = general_purpose::STANDARD.encode(&out);
            serde_json::to_string(&Response {
                ok: true,
                result: json!({
                    "size_after": out.len(),
                    "algo": algo,
                    "data_b64": out_b64
                }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::Hash { data_b64, algo }) => {
            let data = general_purpose::STANDARD.decode(data_b64).unwrap_or_default();
            let hex = match algo.as_str() {
                "sha256" => {
                    let mut h = Sha256::new();
                    h.update(&data);
                    format!("{:x}", h.finalize())
                }
                _ => "".to_string(),
            };
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "algo": algo, "hex": hex }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Err(_) => serde_json::to_string(&Response {
            ok: false,
            result: json!({ "error": "invalid_request" }),
            mode: "".to_string(),
        })
        .unwrap(),
    }
}

