use serde::{Deserialize, Serialize};
use serde_json::{json, Value};

#[derive(Deserialize)]
#[serde(tag = "op")]
pub enum Request {
    #[serde(rename = "infer")]
    Infer { weights: Weights, x: f64 },
    #[serde(rename = "infer_batch")]
    InferBatch { weights: Weights, xs: Vec<f64> },
}

#[derive(Serialize, Deserialize, Clone, Copy)]
pub struct Weights {
    pub w: f64,
    pub b: f64,
}

#[derive(Serialize)]
pub struct Response {
    pub ok: bool,
    pub result: Value,
    pub mode: String,
}

pub fn process_request(input: &str) -> String {
    match serde_json::from_str::<Request>(input) {
        Ok(Request::Infer { weights, x }) => {
            let y = weights.w * x + weights.b;
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "y": y }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::InferBatch { weights, xs }) => {
            let ys: Vec<f64> = xs.iter().map(|x| weights.w * x + weights.b).collect();
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "ys": ys }),
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

