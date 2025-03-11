use safer_ffi::ffi_export;
#[cfg(feature = "headers")]
use safer_ffi::headers::Language;
use safer_ffi::prelude::repr_c;

mod file_dialogs;

#[ffi_export]
fn free_c_string(ptr: repr_c::String) {
    let _ = ptr;
}

#[cfg(feature = "headers")]
pub fn generate_headers() -> ::std::io::Result<()> {
    ::safer_ffi::headers::builder()
        .with_language(Language::C)
        .to_file("target/native_helper.h")?
        .generate()
}
