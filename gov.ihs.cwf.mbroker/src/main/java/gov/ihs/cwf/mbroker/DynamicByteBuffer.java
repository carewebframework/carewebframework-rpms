/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.mbroker;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a byte-oriented buffer that has an initial size and can grow as needed by a specified
 * incremental size. Buffers are allocated internally as a pool of byte arrays. Buffer operations
 * are not thread safe.
 * 
 * 
 */
public class DynamicByteBuffer {
    
    private int chunk; // Index of the current buffer chunk
    
    private final List<byte[]> pool = new ArrayList<byte[]>(); // Buffer chunk pool 
    
    private final int incrementalSize; // Increment in bytes by which buffer will grow beyond initial size
    
    private final int initialSize; // Initial size for buffer
    
    private int position; // Byte offset within current chunk
    
    private int size; // Total number of bytes currently stored in buffer
    
    /**
     * Create a buffer with an initial and incremental size of 100 bytes.
     */
    DynamicByteBuffer() {
        this(100);
    }
    
    /**
     * Create a buffer with the specified initial size. The incremental size will be the same as the
     * initial size.
     * 
     * @param initialSize
     */
    DynamicByteBuffer(int initialSize) {
        this(initialSize, initialSize);
    }
    
    /**
     * Create a buffer with the specified initial and incremental size.
     * 
     * @param initialSize
     * @param incrementalSize
     */
    DynamicByteBuffer(int initialSize, int incrementalSize) {
        this.initialSize = initialSize;
        this.incrementalSize = incrementalSize;
        clear();
    }
    
    /**
     * Activates the next chunk from the pool, allocating it if necessary.
     * 
     * @return The active chunk.
     */
    private byte[] alloc() {
        position = 0;
        chunk++;
        
        if (chunk >= pool.size()) {
            byte[] bytes = new byte[chunk == 0 ? initialSize : incrementalSize];
            pool.add(bytes);
        }
        
        return chunk();
    }
    
    /**
     * Returns the currently active chunk. If no chunk is currently active, allocates the initial
     * chunk and returns it.
     * 
     * @return
     */
    private byte[] chunk() {
        return chunk == -1 ? alloc() : pool.get(chunk);
    }
    
    /**
     * Checks that an index falls within the currently valid range. Raises a run time exception if
     * it does not.
     * 
     * @param index
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * Returns the byte at the specified index.
     * 
     * @param index
     * @return Byte at index.
     * @exception IndexOutOfBoundsException
     */
    public byte get(int index) {
        checkIndex(index);
        return getByte(index);
    }
    
    /**
     * Returns the byte at the specified index. No index validation is performed.
     * 
     * @param index
     * @return
     */
    private byte getByte(int index) {
        if (index < initialSize) {
            return pool.get(0)[index];
        }
        
        index -= initialSize;
        return pool.get(index / incrementalSize + 1)[index % incrementalSize];
    }
    
    /**
     * Writes one or more bytes to the buffer. Values may be specified as multiple arguments or as a
     * byte array.
     * 
     * @param value One or more byte values to be written.
     */
    public void put(byte... value) {
        put(value, value.length);
    }
    
    /**
     * Writes a specified number of bytes from an array, starting at the beginning of the array.
     * 
     * @param bytes Array of bytes to be written.
     * @param count Number of bytes to be written.
     */
    public void put(byte[] bytes, int count) {
        put(bytes, 0, count);
    }
    
    /**
     * Writes a range of bytes from the specified array.
     * 
     * @param bytes Array containing the bytes to be written.
     * @param start Start of the range (inclusive).
     * @param end End of the range (exclusive).
     */
    public void put(byte[] bytes, int start, int end) {
        byte[] buffer = chunk();
        
        for (int i = start; i < end; i++) {
            if (position == buffer.length) {
                buffer = alloc();
            }
            
            buffer[position++] = bytes[i];
            size++;
        }
    }
    
    /**
     * Returns the current contents of the buffer as a byte array.
     * 
     * @return Buffer contents as a byte array.
     */
    public byte[] toArray() {
        byte[] result = new byte[size];
        int pos = 0;
        
        for (byte[] buffer : pool) {
            for (int i = 0; i < buffer.length && pos < size; i++) {
                result[pos++] = buffer[i];
            }
        }
        
        return result;
    }
    
    /**
     * Returns the range of bytes beginning at the start position and exclusive of the end position.
     * If the start position is greater than or equal to the end position, an empty array is
     * returned.
     * 
     * @param start Starting position.
     * @param end Ending position.
     * @return Byte array.
     * @exception IndexOutOfBoundsException
     */
    public byte[] toArray(int start, int end) {
        checkIndex(start);
        checkIndex(end);
        byte[] result = new byte[start > end ? 0 : end - start];
        
        for (int i = start; i < end; i++) {
            result[i] = getByte(i);
        }
        
        return result;
    }
    
    /**
     * Returns the total number of bytes currently stored in the buffer.
     * 
     * @return Bytes in buffer.
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Clears the buffer contents. Does not deallocate any buffer space.
     */
    public void clear() {
        size = 0;
        chunk = -1;
    }
    
    /**
     * Clears the buffer contents and deallocates all buffer space.
     */
    public void release() {
        clear();
        pool.clear();
    }
}
