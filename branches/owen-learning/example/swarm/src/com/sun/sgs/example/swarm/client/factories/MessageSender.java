package com.sun.sgs.example.swarm.client.factories;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.ClientChannel;

import com.sun.sgs.example.swarm.shared.messages.Message;

/**
 *
 * @author ok194946
 */
public class MessageSender
{
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(MessageSender.class.getName());
    
    public static void sendMessage(Message message, SimpleClient session)
    {
        try {
            //serialize the message
            ByteBuffer buffer = serializeMessage(message);

            //send the message
            session.send(buffer);
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unable to send result message", e);
        }
    }
    
    public static void sendResult(Message message, ClientChannel channel)
    {
        try {
            //serialize the message
            ByteBuffer buffer = serializeMessage(message);

            //send the message
            channel.send(buffer);
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unable to process message", e);
        }
    }
    
    private static ByteBuffer serializeMessage(Message message) throws Exception
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byteStream);
        output.writeObject(message);
        
        return ByteBuffer.wrap(byteStream.toByteArray());
    }
    
}
