use once_cell::sync::Lazy;
use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
use std::collections::HashMap;
use std::sync::Mutex;

#[derive(Serialize)]
pub struct Response {
    pub ok: bool,
    pub result: Value,
    pub mode: String,
}

#[derive(Default)]
struct MemoryStore {
    next_id: usize,
    map: HashMap<usize, Vec<u8>>,
}

static STORE: Lazy<Mutex<MemoryStore>> = Lazy::new(|| Mutex::new(MemoryStore::default()));

#[derive(Deserialize)]
#[serde(tag = "op")]
pub enum Request {
    #[serde(rename = "alloc")]
    Alloc { size: usize },
    #[serde(rename = "write")]
    Write { id: usize, data: String },
    #[serde(rename = "read")]
    Read { id: usize },
    #[serde(rename = "free")]
    Free { id: usize },
    #[serde(rename = "io")]
    Io { kind: String, data: String },
}

pub fn process_request(input: &str) -> String {
    match serde_json::from_str::<Request>(input) {
        Ok(Request::Alloc { size }) => {
            let mut s = STORE.lock().unwrap();
            s.next_id += 1;
            let id = s.next_id;
            s.map.insert(id, vec![0u8; size]);
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "id": id }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::Write { id, data }) => {
            if let Ok(bytes) = base64::decode(data) {
                let mut s = STORE.lock().unwrap();
                s.map.insert(id, bytes);
                serde_json::to_string(&Response {
                    ok: true,
                    result: json!({}),
                    mode: "".to_string(),
                })
                .unwrap()
            } else {
                serde_json::to_string(&Response {
                    ok: false,
                    result: json!({ "error": "invalid_base64" }),
                    mode: "".to_string(),
                })
                .unwrap()
            }
        }
        Ok(Request::Read { id }) => {
            let s = STORE.lock().unwrap();
            let data = s.map.get(&id).cloned().unwrap_or_default();
            let b64 = base64::encode(data);
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "data": b64 }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::Free { id }) => {
            let mut s = STORE.lock().unwrap();
            s.map.remove(&id);
            serde_json::to_string(&Response {
                ok: true,
                result: json!({}),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::Io { kind, data }) => {
            let len = data.len();
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "kind": kind, "len": len }),
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
