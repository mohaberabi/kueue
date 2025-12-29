package com.mohaberabi.kline.utility

/**
 * A class to buffer the upcoming bytes
 * @param initialCapacity the initial capacity for your byte array
 * @property buffer the actual byte array
 * @property writtenBytes think of it as your size of [buffer] how many bytes are written is logically = the size of your bytes
 *
 */
class BytesOutputStream(
    private val initialCapacity: Int = 1024
) {

    private var buffer = ByteArray(initialCapacity)
    private var writtenBytes = 0

    /**
     * wrapper of [write(src,offset,length)] means append all from start to end
     */
    fun write(
        src: ByteArray,
    ) {
        write(
            src = src,
            offset = 0,
            length = src.size
        )
    }

    /**
     * returns the size of written bytes
     */
    fun size() = writtenBytes

    /**
     * writes the upcoming bytes into the [buffer] at the end of it
     * this function grows if needed by doupling the current capacity once you hit a small capacity to write
     * if the caller wants to append an empty bytes -> return
     * throws [IllegalArgumentException] if the offset is not positive
     * throws [IllegalArgumentException] if the length is not positive
     * throws [IllegalArgumentException] if the slice to be written exceeds the actual [src] size
     * [ensureCapacity] this where we double our size until we have enough capacity
     * then we are basically appending the upcoming to our buffer or our new buffered with enough  capacity
     * takes bytes from [src] to our [buffer] ---> src[offset]...src[offset+length-1]
     * [startIndex] inclusive and [endIndex] is exclusive
     */
    fun write(
        src: ByteArray,
        offset: Int,
        length: Int
    ) {
        if (length <= 0) return
        require(offset >= 0) { "offset must be >= 0" }
        require(length >= 0) { "length must be >= 0" }
        require(offset + length <= src.size) { "offset+length out of bounds" }
        ensureCapacity(writtenBytes + length)
        src.copyInto(
            destination = buffer,
            destinationOffset = writtenBytes,
            startIndex = offset,
            endIndex = offset + length
        )
        writtenBytes += length
    }

    /** returns the index of [byte] */
    fun indexOf(byte: Byte) = buffer.indexOf(byte)


    /**
     * special method to return the index of the pattern , like what exactly in string matching
     * but in bytes think of it like
     * example : Hello.indexOfPattern(He) -> 0 we needed it in the printer sockets to extract a range where creates a command
     * if pattern is empty -> return 0
     * if pattern is longer than our buffer means we will never be able to find the index -> return -1
     *
     */
    fun indexOfPattern(pattern: ByteArray): Int {
        if (pattern.isEmpty()) return 0
        if (pattern.size > writtenBytes) return -1
        val limit = writtenBytes - pattern.size
        outer@ for (i in 0..limit) {
            for (j in pattern.indices) {
                if (buffer[i + j] != pattern[j]) continue@outer
            }
            return i
        }

        return -1
    }

    /**
     * pops all written bytes starting from the start index = [0] until [count-1]
     * basically means get me all the [N] bytes in this buffer from start
     * throws [IllegalArgumentException] if count is negative means return me nothing
     * or count is actually a number exceeds the number of bytes we already wrote in memory
     * returns empty byte array if [count] = 0
     * we store the number of bytes we need to return in a new array [0..count-1]
     * [remaining] is number of bytes remained after popping the bytes
     * if our written bytes = 10 and we requested to pop 9 bytes remaining should be = 1
     * if we still yet have any bytes remaining we need to create a new copy of the [buffer] with remaining bytes
     * we basically shift the left bytes into the start of the array from 0 until left
     *
     */
    fun popFromTop(
        count: Int,
    ): ByteArray {
        require(count >= 0 && count <= writtenBytes) { "index out of bounds " }
        if (count == 0) return ByteArray(0)
        val output = buffer.copyOfRange(fromIndex = 0, toIndex = count)
        val remaining = writtenBytes - count
        if (remaining > 0) {
            buffer.copyInto(
                destination = buffer,
                destinationOffset = 0,
                startIndex = count,
                endIndex = writtenBytes,
            )
        }
        writtenBytes = remaining
        return output
    }

    /**
     * setting the [writtenBytes] = 0 , this means next time we append we append from start fresh
     */
    fun clear() {
        writtenBytes = 0
    }

    /**
     * returns our [buffer] as a [ByteArray]
     */
    fun toByteArray() = buffer

    /**
     *  if [required] size can fit inside our [buffer] do not do anything
     * create a new variable [newSize] that initially holds our [buffer] size
     * keeps looping until the [newSize] is smaller than the [required]
     * look the [required] here is your latest buffer size + new required to be appended bytes size
     * each iteration we double size until we have enough capacity
     * after we are sure that our size can now fit we
     * [ByteArray.copyOf] Returns new array which is a copy of the original array, resized to the given
     * so basically we are here ensuring size fits , getting this size and making it the new size of our new copy of ByteArray[buffer]
     */
    private fun ensureCapacity(required: Int) {
        if (required <= buffer.size) return
        var newSize = buffer.size
        while (newSize < required) newSize *= 2
        buffer = buffer.copyOf(newSize = newSize)
    }
}