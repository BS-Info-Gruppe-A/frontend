package eu.bsinfo.native_helper

import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import eu.bsinfo.native_helper.generated.NativeHelper
import eu.bsinfo.native_helper.generated.slice_ref_Filter
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
    CFilter.spec(segment, allocator.allocateStrings(spec))
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
    @Suppress("TYPE_MISMATCH") // For some reason, the IDE gets this wrong
    return readString().ifBlank { throw FileDialogCancelException() }
}
