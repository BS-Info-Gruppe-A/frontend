use rfd::FileDialog;
use safer_ffi::prelude::*;
use std::path::PathBuf;

#[derive_ReprC]
#[repr(C)]
struct Filter<'a> {
    name: repr_c::String,
    spec: c_slice::Ref<'a, repr_c::String>
}

type Filters<'a> = c_slice::Ref<'a, Filter<'a>>;

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
        dialog.add_filter(filter.name.clone(), &filter.spec)
    })
}

fn path_to_string(path: Option<PathBuf>) -> repr_c::String {
    path.map(|path| path.to_str().map(repr_c::String::from))
        .flatten()
        .unwrap_or(repr_c::String::from(""))
}
