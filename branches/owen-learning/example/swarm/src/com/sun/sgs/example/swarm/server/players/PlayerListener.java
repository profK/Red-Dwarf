package com.sun.sgs.example.swarm.server.players;

import java.util.Map;
import java.io.Serializable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ManagedObject;

import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.domain.Room;
import com.sun.sgs.example.swarm.server.Constants;
import com.sun.sgs.example.swarm.shared.messages.Message;
import com.sun.sgs.example.swarm.server.factories.MessageTaskFactory;
import com.sun.sgs.example.swarm.shared.util.ByteBufferInputStream;

/**
 * The session listener used to receive incoming messages from each connected
 * player.  This class is intended to be a conduit for all incoming messages
 * from a player.  Each message results in a task spawned to be done at some
 * point in the future (if applicable).
 */
public class PlayerListener implements ClientSessionListener, ManagedObject, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(PlayerListener.class.getName());
    
    /** The session this {@code ClientSessionListener} is listening to. */
    private final ManagedReference<ClientSession> sessionRef;
    
    /** The Player object that this player is controlling */
    private ManagedReference<Player> playerRef;
    
    /** The Room that this player is in */
    private ManagedReference<Room> roomRef;

    
    /** Creates a new instance of PlayerListener */
    public PlayerListener(ClientSession session)
    {
        sessionRef = AppContext.getDataManager().createReference(session);
        
        //player is not created until user sends a join lobby request
        playerRef = null;
        roomRef = null;
    }
    
    public ManagedReference<ClientSession> getSessionRef() { return sessionRef; }
    
    public ManagedReference<Player> getPlayerRef() { return playerRef; }
    public void setPlayerRef(ManagedReference<Player> playerRef) { this.playerRef = playerRef; }
    
    public ManagedReference<Room> getRoomRef() { return roomRef; }
    public void setRoomRef(ManagedReference<Room> roomRef) { this.roomRef = roomRef; }
    
    public void receivedMessage(ByteBuffer message)
    {
        try {
            //deserialize the message
            ObjectInputStream input = new ObjectInputStream(new ByteBufferInputStream(message));
            Message m = (Message)input.readObject();
            
            //process the message
            ManagedReference<PlayerListener> playerRef = AppContext.getDataManager().createReference(this);
            AppContext.getTaskManager().scheduleTask(MessageTaskFactory.generateTask(m, playerRef));
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unable to process message", e);
        }
        
    }
    
    public void disconnected(boolean graceful)
    {
        //remove player from current room and notify other clients
        Map<String, Room> userMap = (Map<String, Room>)AppContext.getDataManager().getBinding(Constants.USER_LIST);
        Room playerRoom = userMap.get(sessionRef.get().getName());
        if(playerRoom != null) {
            playerRoom.leaveRoom(playerRef.get(), sessionRef.get());
        }
    }

}
