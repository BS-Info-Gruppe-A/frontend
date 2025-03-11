use rfd::FileDialog;
use safer_ffi::prelude::*;
use std::path::PathBuf;

#[derive_ReprC]
#[repr(C)]
struct Filter {
    name: repr_c::String,
    spec: repr_c::String,
}

#[derive_ReprC]
#[repr(C)]
struct FileDialogResponse {
    is_error: bool,
    file: Option<repr_c::String>,
}

type Filters<'a> = c_slice::Ref<'a, Filter>;

#[ffi_export]
fn fd_is_err(result: Option<repr_c::String>)-> bool {
    result.is_none()
}

#[ffi_export]
fn open_file(filters: Filters<'_>) -> repr_c::String {
    path_to_string(file_picker(filters).pick_file())
}

#[ffi_export]
fn save_file(filters: Filters<'_>) -> repr_c::String {
    path_to_string(file_picker(filters).save_file())
}

fn file_picker(filters: Filters<'_>) -> FileDialog {
    filters.iter().fold(FileDialog::new(), |dialog, filter| {
        dialog.add_filter(filter.name.clone(), &[filter.spec.clone()])
    })
}

fn path_to_string(path: Option<PathBuf>) -> repr_c::String {
    path.map(|path| path.to_str().map(repr_c::String::from))
        .flatten()
        .unwrap_or(repr_c::String::from(""))
}
