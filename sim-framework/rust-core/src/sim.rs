use serde::{Deserialize, Serialize};
use serde_json::{json, Value};

#[derive(Serialize, Deserialize, Clone)]
pub struct Body {
    pub x: f64,
    pub y: f64,
    pub vx: f64,
    pub vy: f64,
    pub r: f64,
    pub mass: f64,
}

#[derive(Serialize, Deserialize, Clone)]
pub struct World {
    pub width: f64,
    pub height: f64,
    pub bodies: Vec<Body>,
}

#[derive(Serialize, Deserialize)]
#[serde(tag = "op")]
pub enum Request {
    #[serde(rename = "step")]
    Step { world: World, dt: f64 },
    #[serde(rename = "metrics")]
    Metrics { world: World },
}

#[derive(Serialize)]
pub struct Response {
    pub ok: bool,
    pub result: Value,
    pub mode: String,
}

pub fn process_request(input: &str) -> String {
    match serde_json::from_str::<Request>(input) {
        Ok(Request::Step { mut world, dt }) => {
            let mut events = Vec::<Value>::new();
            step_world(&mut world, dt, &mut events);
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "world": world, "events": events }),
                mode: "".to_string(),
            })
            .unwrap()
        }
        Ok(Request::Metrics { world }) => {
            let k = total_kinetic_energy(&world);
            let count = world.bodies.len() as u64;
            serde_json::to_string(&Response {
                ok: true,
                result: json!({ "bodies": count, "kinetic_energy": k }),
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

fn step_world(world: &mut World, dt: f64, events: &mut Vec<Value>) {
    let w = world.width;
    let h = world.height;
    for b in &mut world.bodies {
        b.x += b.vx * dt;
        b.y += b.vy * dt;
        if b.x - b.r < 0.0 {
            b.x = b.r;
            b.vx = -b.vx;
            events.push(json!({"type":"boundary_bounce","side":"left"}));
        }
        if b.x + b.r > w {
            b.x = w - b.r;
            b.vx = -b.vx;
            events.push(json!({"type":"boundary_bounce","side":"right"}));
        }
        if b.y - b.r < 0.0 {
            b.y = b.r;
            b.vy = -b.vy;
            events.push(json!({"type":"boundary_bounce","side":"top"}));
        }
        if b.y + b.r > h {
            b.y = h - b.r;
            b.vy = -b.vy;
            events.push(json!({"type":"boundary_bounce","side":"bottom"}));
        }
    }
    let n = world.bodies.len();
    for i in 0..n {
        let (left, right) = world.bodies.split_at_mut(i + 1);
        let a = &mut left[i];
        for (off, b) in right.iter_mut().enumerate() {
            let j = i + 1 + off;
            let dx = b.x - a.x;
            let dy = b.y - a.y;
            let dist2 = dx * dx + dy * dy;
            let rsum = a.r + b.r;
            if dist2 <= rsum * rsum {
                resolve_collision(a, b);
                events.push(json!({"type":"collision","i":i,"j":j}));
            }
        }
    }
}

fn resolve_collision(a: &mut Body, b: &mut Body) {
    let nx = b.x - a.x;
    let ny = b.y - a.y;
    let dist = (nx * nx + ny * ny).sqrt();
    if dist == 0.0 {
        return;
    }
    let nx = nx / dist;
    let ny = ny / dist;
    let dvx = b.vx - a.vx;
    let dvy = b.vy - a.vy;
    let rel_vel = dvx * nx + dvy * ny;
    if rel_vel > 0.0 {
        return;
    }
    let m1 = a.mass;
    let m2 = b.mass;
    let e = 1.0;
    let j = -(1.0 + e) * rel_vel / (1.0 / m1 + 1.0 / m2);
    let ix = j * nx;
    let iy = j * ny;
    a.vx -= ix / m1;
    a.vy -= iy / m1;
    b.vx += ix / m2;
    b.vy += iy / m2;
}

fn total_kinetic_energy(world: &World) -> f64 {
    let mut k = 0.0;
    for b in &world.bodies {
        let v2 = b.vx * b.vx + b.vy * b.vy;
        k += 0.5 * b.mass * v2;
    }
    k
}
