use rand::{rngs::StdRng, Rng, SeedableRng};
use serde::{Deserialize, Serialize};
use serde_json::{json, Value};

#[derive(Deserialize)]
#[serde(tag = "op")]
pub enum Request {
    #[serde(rename = "sum")]
    Sum { values: Vec<f64> },
    #[serde(rename = "simulate")]
    Simulate { steps: u64, seed: u64 },
}

#[derive(Serialize)]
pub struct Response {
    pub ok: bool,
    pub result: Value,
    pub mode: String,
}

pub fn process_request(input: &str) -> String {
    let parsed: Result<Request, _> = serde_json::from_str(input);
    match parsed {
        Ok(Request::Sum { values }) => {
            let sum: f64 = values.iter().copied().sum();
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "sum": sum }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::Simulate { steps, seed }) => {
            let mut rng = StdRng::seed_from_u64(seed);
            let mut acc = 0.0f64;
            for _ in 0..steps {
                let x: f64 = rng.gen::<f64>() - 0.5;
                acc += x;
            }
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "acc": acc, "steps": steps, "seed": seed }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Err(_) => {
            serde_json::to_string(&Response {
                ok: false,
                result: json!({ "error": "invalid_request" }),
                mode: "".to_string(),
            })
            .unwrap()
        }
    }
}

