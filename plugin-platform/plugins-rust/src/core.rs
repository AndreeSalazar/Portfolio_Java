use serde::{Deserialize, Serialize};
use serde_json::{json, Value};

#[derive(Deserialize)]
#[serde(tag = "op")]
pub enum Request {
    #[serde(rename = "echo")]
    Echo { msg: String },
    #[serde(rename = "sum")]
    Sum { values: Vec<f64> },
}

#[derive(Serialize)]
pub struct Response {
    pub ok: bool,
    pub result: Value,
    pub mode: String,
}

pub fn process_request(input: &str) -> String {
    match serde_json::from_str::<Request>(input) {
        Ok(Request::Echo { msg }) => serde_json::to_string(&Response {
            ok: true,
            result: json!({ "msg": msg }),
            mode: "".to_string(),
        })
        .unwrap(),
        Ok(Request::Sum { values }) => {
            let sum: f64 = values.iter().copied().sum();
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "sum": sum }),
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

