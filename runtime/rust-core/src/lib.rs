pub mod core;

use std::ffi::{CStr, CString};
use std::os::raw::c_char;

use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;

#[no_mangle]
pub extern "C" fn rc_execute(json_ptr: *const c_char) -> *mut c_char {
    if json_ptr.is_null() {
        return std::ptr::null_mut();
    }
    let c_str = unsafe { CStr::from_ptr(json_ptr) };
    let input = c_str.to_str().unwrap_or("");
    let mut out = core::process_request(input);
    out = stamp_mode(out, "JNI");
    let s = CString::new(out).unwrap();
    s.into_raw()
}

#[no_mangle]
pub extern "C" fn rc_free(ptr: *mut c_char) {
    if ptr.is_null() {
        return;
    }
    unsafe { let _ = CString::from_raw(ptr); }
}

#[no_mangle]
pub extern "system" fn Java_runtime_NativeRuntime_rcExecuteJNI(
    mut env: JNIEnv,
    _class: JClass,
    request: JString,
) -> jstring {
    let input: String = env
        .get_string(&request)
        .map(|s| s.to_string_lossy().into_owned())
        .unwrap_or_default();
    let mut out = core::process_request(&input);
    out = stamp_mode(out, "JNI");
    let j_out = env.new_string(out).unwrap();
    j_out.into_raw()
}

pub fn stamp_mode(mut json: String, mode: &str) -> String {
    if let Ok(mut v) = serde_json::from_str::<serde_json::Value>(&json) {
        if let Some(obj) = v.as_object_mut() {
            obj.insert("mode".to_string(), serde_json::Value::String(mode.to_string()));
        }
        if let Ok(s) = serde_json::to_string(&v) {
            json = s;
        }
    }
    json
}
