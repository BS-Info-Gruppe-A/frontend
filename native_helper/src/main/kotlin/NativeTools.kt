package eu.bsinfo.native_helper

import eu.bsinfo.native_helper.generated.NativeHelper
import eu.bsinfo.native_helper.generated.Vec_uint8
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator

internal fun SegmentAllocator.allocateCString(
    value: String,
    segment: MemorySegment = Vec_uint8.allocate(this)
): MemorySegment = segment.apply {
    val ptr = allocateFrom(value)
    val len = value.length.toLong()

    Vec_uint8.len(segment, len)
    Vec_uint8.cap(segment, ptr.byteSize())
    Vec_uint8.ptr(segment, ptr)
}

internal fun MemorySegment.readString(): String {
    try {
        val ptr = Vec_uint8.ptr(this)
        val vectorLength = Vec_uint8.len(this)
        val bytes = ptr.reinterpret(vectorLength).asByteBuffer()

        return Charsets.UTF_8.decode(bytes).toString()
    } finally {
        NativeHelper.free_c_string(this)
    }
}
