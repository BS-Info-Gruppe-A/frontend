package eu.bsinfo.native_helper

import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import eu.bsinfo.native_helper.generated.NativeHelper
import eu.bsinfo.native_helper.generated.Vec_uint8
import eu.bsinfo.native_helper.generated.slice_ref_Filter
import eu.bsinfo.native_helper.generated.slice_ref_Vec_uint8
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import eu.bsinfo.native_helper.generated.Filter as CFilter

fun Collection<Filter>.allocate(allocator: SegmentAllocator): MemorySegment =
    slice_ref_Filter.allocate(allocator).apply {
        val array = CFilter.allocateArray(size.toLong(), allocator)
        forEachIndexed { index, value ->
            val entry = array.asSlice(index.toLong() * CFilter.sizeof(), CFilter.sizeof())

            value.allocate(allocator, entry)
        }

        slice_ref_Filter.len(this, size.toLong())
        slice_ref_Filter.ptr(this, array)
    }

fun Filter.allocate(allocator: SegmentAllocator, segment: MemorySegment = CFilter.allocate(allocator)): MemorySegment {
    CFilter.name(segment, allocator.allocateCString(name))
    val spec = slice_ref_Vec_uint8.allocate(allocator).apply {
        val array = Vec_uint8.allocateArray(spec.size.toLong(), allocator)
        slice_ref_Vec_uint8.len(this, spec.size.toLong())
        slice_ref_Vec_uint8.ptr(this, array)

        spec.forEachIndexed { index, value ->
            val entry = array.asSlice(index.toLong() * Vec_uint8.sizeof(), Vec_uint8.sizeof())

            allocator.allocateCString(value, entry)
        }
    }
    CFilter.spec(segment, spec)
    return segment
}


fun openFile(vararg filters: Filter) = Arena.ofConfined().use {
    val cFilters = filters.asList().allocate(it)

    NativeHelper.open_file(it, cFilters).readFilePath()
}

fun saveFile(vararg filters: Filter) = Arena.ofConfined().use {
    val cFilters = filters.asList().allocate(it)

    NativeHelper.save_file(it, cFilters).readFilePath()
}

private fun MemorySegment.readFilePath(): String {
    return readString().ifBlank { throw FileDialogCancelException() }
}
