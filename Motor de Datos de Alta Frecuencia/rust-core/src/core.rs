use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
use memchr::memchr_iter;

#[derive(Deserialize)]
#[serde(tag = "op")]
pub enum Request {
    #[serde(rename = "parse")]
    Parse { csv: String },
}

#[derive(Serialize)]
pub struct Response {
    pub ok: bool,
    pub result: Value,
    pub mode: String,
}

pub fn process_request(input: &str) -> String {
    match serde_json::from_str::<Request>(input) {
        Ok(Request::Parse { csv }) => {
            let m = parse_line(&csv);
            serde_json::to_string(&Response {
                ok: true,
                result: json!(m),
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

#[derive(Serialize)]
struct Metrics {
    ts: u64,
    symbol: String,
    price: f64,
    qty: u64,
    notional: f64,
}

fn parse_line(s: &str) -> Metrics {
    let mut fields = Vec::with_capacity(8);
    let mut last = 0;
    for pos in memchr_iter(b',', s.as_bytes()) {
        fields.push(&s[last..pos]);
        last = pos + 1;
    }
    fields.push(&s[last..]);
    let ts: u64 = fields.get(0).and_then(|x| x.parse().ok()).unwrap_or(0);
    let symbol: String = fields.get(1).map(|x| x.to_string()).unwrap_or_default();
    let price: f64 = fields.get(2).and_then(|x| x.parse().ok()).unwrap_or(0.0);
    let qty: u64 = fields.get(3).and_then(|x| x.parse().ok()).unwrap_or(0);
    let notional = price * (qty as f64);
    Metrics { ts, symbol, price, qty, notional }
}

