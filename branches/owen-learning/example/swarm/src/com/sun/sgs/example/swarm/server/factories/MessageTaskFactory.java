package com.sun.sgs.example.swarm.server.factories;

import com.sun.sgs.app.ManagedReference;

import com.sun.sgs.example.swarm.server.tasks.MessageTask;
import com.sun.sgs.example.swarm.server.tasks.AttackTask;
import com.sun.sgs.example.swarm.server.tasks.BuildTask;
import com.sun.sgs.example.swarm.server.tasks.JoinGameTask;
import com.sun.sgs.example.swarm.server.tasks.JoinLobbyTask;
import com.sun.sgs.example.swarm.server.tasks.MoveTask;
import com.sun.sgs.example.swarm.shared.messages.Message;
import com.sun.sgs.example.swarm.shared.messages.AttackMessage;
import com.sun.sgs.example.swarm.shared.messages.BuildMessage;
import com.sun.sgs.example.swarm.shared.messages.JoinGameMessage;
import com.sun.sgs.example.swarm.shared.messages.JoinLobbyMessage;
import com.sun.sgs.example.swarm.shared.messages.MoveMessage;
import com.sun.sgs.example.swarm.server.players.PlayerListener;


/**
 *
 * @author owen
 */
public class MessageTaskFactory 
{
    public static MessageTask generateTask(Message message, ManagedReference<PlayerListener> player)
    {
        if(message instanceof JoinLobbyMessage)
            return new JoinLobbyTask((JoinLobbyMessage)message, player);
        else if(message instanceof JoinGameMessage)
            return new JoinGameTask((JoinGameMessage)message, player);
        else if(message instanceof AttackTask)
            return new AttackTask((AttackMessage)message, player);
        else if(message instanceof BuildTask)
            return new BuildTask((BuildMessage)message, player);
        else if(message instanceof MoveTask)
            return new MoveTask((MoveMessage)message, player);
        
        return null;
    }

}
