package com.sun.sgs.example.swarm.server.factories;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.Channel;

import com.sun.sgs.example.swarm.shared.results.Result;

/**
 *
 * @author ok194946
 */
public class ResultSender
{
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(ResultSender.class.getName());
    
    public static void sendResult(Result result, ClientSession client)
    {
        try {
            //serialize the message
            ByteBuffer buffer = serializeResult(result);

            //send the message
            client.send(buffer);
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unable to send result message", e);
        }
    }
    
    public static void sendResult(Result result, Channel channel)
    {
        try {
            //serialize the message
            ByteBuffer buffer = serializeResult(result);

            //send the message
            channel.send(null, buffer);
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unable to process message", e);
        }
    }
    
    private static ByteBuffer serializeResult(Result result) throws Exception
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byteStream);
        output.writeObject(result);
        
        return ByteBuffer.wrap(byteStream.toByteArray());
    }
    
}
