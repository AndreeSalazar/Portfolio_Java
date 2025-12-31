use std::io::{BufRead, BufReader, Write};
use std::net::{TcpListener, TcpStream};

fn handle(mut stream: TcpStream) {
    let reader = BufReader::new(stream.try_clone().unwrap());
    for line in reader.lines() {
        if let Ok(req) = line {
            let mut out = sim_core::sim::process_request(&req);
            out = sim_core::stamp_mode(out, "IPC");
            let _ = writeln!(stream, "{}", out);
        } else {
            break;
        }
    }
    let _ = stream.shutdown(std::net::Shutdown::Both);
}

fn main() {
    let listener = TcpListener::bind("127.0.0.1:9091").unwrap();
    for conn in listener.incoming() {
        if let Ok(stream) = conn {
            std::thread::spawn(|| handle(stream));
        }
    }
}

