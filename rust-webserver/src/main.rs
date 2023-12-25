use std::{
    collections::HashMap,
    fmt::{self},
    io::{prelude::*, BufReader},
    net::{TcpListener, TcpStream},
    sync::{Arc, RwLock, RwLockWriteGuard},
    thread,
};

enum Value {
    Int(i64),
    Float(f64),
    String(String),
}

impl fmt::Display for Value {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Value::Int(i) => write!(f, "{}", i),
            Value::Float(float) => write!(f, "{}", float),
            Value::String(string) => write!(f, "{}", string),
        }
    }
}

fn main() {
    let key_value_store: Arc<RwLock<HashMap<i64, Value>>> = Arc::new(RwLock::new(HashMap::new()));
    let listener = TcpListener::bind("127.0.0.1:7878").unwrap();

    for stream in listener.incoming() {
        let stream = stream.unwrap();
        let cloned_kv_store = Arc::clone(&key_value_store);
        thread::spawn(move || {
            let this_map = cloned_kv_store.write().expect("msg");
            handle_connection(stream, this_map);
        });
    }
}

fn handle_connection(
    mut stream: TcpStream,
    mut key_value_store: RwLockWriteGuard<HashMap<i64, Value>>,
) {
    let buf_reader = BufReader::new(&mut stream);
    let http_request: Vec<_> = buf_reader
        .lines()
        .map(|result| result.unwrap())
        .take_while(|line| !line.is_empty())
        .collect();

    let request_params: Vec<&str> = http_request.get(0).unwrap().split(' ').collect();
    let method = request_params.get(0).unwrap();
    let path = request_params.get(1).unwrap();

    println!("{} {}", method, path);

    let mut response = String::from("HTTP/1.1 ");
    match method.as_ref() {
        "GET" => {
            response.push_str("200 OK\r\n\r\n");
            response.push_str(&map_to_string(&key_value_store));
        }
        "PUT" => {
            let kv_pair = parse_query(path.to_string());
            let key = kv_pair.0.parse::<i64>().unwrap();
            if !kv_pair.1.parse::<i64>().is_err() {
                let value = kv_pair.1.parse::<i64>().unwrap();
                let _ = &key_value_store.insert(key, Value::Int(value));
            } else if !kv_pair.1.parse::<f64>().is_err() {
                let value = kv_pair.1.parse::<f64>().unwrap();
                let _ = &key_value_store.insert(key, Value::Float(value));
            } else {
                let _ = &key_value_store.insert(key, Value::String(kv_pair.1));
            }
            response.push_str("200 OK\r\n\r\n");
        }
        _ => {
            response.push_str("404 Bad request\r\n\r\n");
        }
    }

    stream.write_all(response.as_bytes()).unwrap();
}

fn map_to_string(data: &HashMap<i64, Value>) -> String {
    return data
        .iter()
        .map(|(k, v)| format!("{}={}", k, v))
        .collect::<Vec<_>>()
        .join(",");
}

fn parse_query(path: String) -> (String, String) {
    let query_vec = path.split('?').collect::<Vec<_>>();
    let query = query_vec.get(1).unwrap();
    let pair_iterator = query.split("&");
    let mut key_param: String = "".to_string();
    let mut value_param: String = "".to_string();
    for pair in pair_iterator {
        let split_pair = pair.split('=').collect::<Vec<&str>>();

        if split_pair.get(0).unwrap().to_string() == "key" {
            key_param = split_pair.get(1).unwrap().to_string();
        }

        if split_pair.get(0).unwrap().to_string() == "value" {
            value_param = split_pair.get(1).unwrap().to_string();
        }
    }
    return (key_param, value_param);
}
