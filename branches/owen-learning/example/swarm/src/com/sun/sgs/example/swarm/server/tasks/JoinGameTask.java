package com.sun.sgs.example.swarm.server.tasks;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ClientSession;

import com.sun.sgs.example.swarm.shared.messages.JoinGameMessage;
import com.sun.sgs.example.swarm.server.players.PlayerListener;
import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.domain.LobbyRoom;
import com.sun.sgs.example.swarm.server.domain.GameRoom;
import com.sun.sgs.example.swarm.server.Constants;
import com.sun.sgs.example.swarm.server.factories.ResultSender;
import com.sun.sgs.example.swarm.server.factories.DataTransformer;
import com.sun.sgs.example.swarm.shared.results.SnapshotResult;


/**
 * joins a player to a game
 */
public class JoinGameTask extends MessageTask<JoinGameMessage> implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(JoinGameTask.class.getName());
    
    /** Creates a new instance of JoinGameTask */
    public JoinGameTask(JoinGameMessage message, ManagedReference<PlayerListener> playerRef)
    {
        super(message, playerRef);
    }

    public void run()
    {
        //retrieve the requested from the message
        LobbyRoom lobby = (LobbyRoom)AppContext.getDataManager().getBinding(Constants.LOBBY);
        GameRoom game = lobby.getRoom(getMessage().getRoom()).getForUpdate();
        
        //determine the player and session objects
        PlayerListener playerListener = getPlayerRef().get();
        Player player = playerListener.getPlayerRef().get();
        ClientSession session = playerListener.getSessionRef().get();
        
        //add the player to the room
        game.joinRoom(player, session);
        
        logger.log(Level.INFO, "Player "+player.getUid()+" joined Game Room "+String.valueOf(getMessage().getRoom()));
        ResultSender.sendResult(new SnapshotResult(DataTransformer.generateGameRoomDTO(game)), session);
    }
}
