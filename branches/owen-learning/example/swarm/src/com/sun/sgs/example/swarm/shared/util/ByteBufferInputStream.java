package com.sun.sgs.example.swarm.shared.util;

import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.IOException;

/**
 *
 * @author ok194946
 */
public class ByteBufferInputStream extends InputStream
{
    private ByteBuffer buffer;
    
    /** Creates a new instance of ByteBufferInputStream */
    public ByteBufferInputStream(ByteBuffer buffer)
    {
        this.buffer = buffer;
    }
    
    public synchronized int read() throws IOException
    {
        if(!buffer.hasRemaining())
            return -1;
        return buffer.get();
    }
    
    public synchronized int read(byte[] bytes, int off, int len) throws IOException
    {
        len = Math.min(len, buffer.remaining());
        buffer.get(bytes, off, len);
        return len;
    }
    
}
