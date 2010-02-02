package com.sun.sgs.example.swarm.server.tasks;

import java.io.Serializable;

import com.sun.sgs.app.Task;
import com.sun.sgs.app.ManagedReference;

import com.sun.sgs.example.swarm.shared.messages.Message;
import com.sun.sgs.example.swarm.server.players.PlayerListener;

/**
 * Generic task for the Swarm app
 */
public abstract class MessageTask <T extends Message> implements Task, Serializable
{
    private T message;
    private ManagedReference<PlayerListener> playerRef;
    
    public MessageTask(T message, ManagedReference<PlayerListener> playerRef)
    {
        this.message = message;
        this.playerRef = playerRef;
    }

    public T getMessage() { return message; }
    public ManagedReference<PlayerListener> getPlayerRef() { return playerRef; }
    
}
