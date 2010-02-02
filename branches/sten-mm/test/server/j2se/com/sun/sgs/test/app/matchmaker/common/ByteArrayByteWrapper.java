package com.sun.sgs.test.app.matchmaker.common;

import java.nio.ByteBuffer;

/**
 * A ByteWrapper that wraps a byte array.
 */
public class ByteArrayByteWrapper implements ByteWrapper {

    private byte[] array;
    
    public ByteArrayByteWrapper(byte[] array) {
        this.array = array;
    }
    
    public int getLength() {
        return array.length + 4;
    }

    public UnsignedByte getType() {
        return new UnsignedByte(TYPE_BYTE_ARRAY);
    }

    public Object getValue() {
        return array;
    }

    public void writeBytes(ByteBuffer buffer) {
        buffer.putInt(array.length);
        buffer.put(array);
    }
    
    public String toString() {
        return "ByteArrayByteWrapper: " + array.toString();
    }
    
    public int hashCode() {
        return array.hashCode();
    }
    
    public boolean equals(Object o) {
        return o instanceof 
                        ByteArrayByteWrapper && o.toString().equals(toString());
    }

}
