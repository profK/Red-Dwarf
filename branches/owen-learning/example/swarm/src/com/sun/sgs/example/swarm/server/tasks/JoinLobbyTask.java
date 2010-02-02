package com.sun.sgs.example.swarm.server.tasks;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ClientSession;

import com.sun.sgs.example.swarm.shared.messages.JoinLobbyMessage;
import com.sun.sgs.example.swarm.server.players.PlayerListener;
import com.sun.sgs.example.swarm.server.factories.PlayerFactory;
import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.Constants;
import com.sun.sgs.example.swarm.server.domain.LobbyRoom;
import com.sun.sgs.example.swarm.server.factories.ResultSender;
import com.sun.sgs.example.swarm.server.factories.DataTransformer;
import com.sun.sgs.example.swarm.shared.results.SnapshotResult;

/**
 * Joins the player to the lobby
 */
public class JoinLobbyTask extends MessageTask<JoinLobbyMessage> implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(JoinLobbyTask.class.getName());
    
    /** Creates a new instance of JoinLobbyTask */
    public JoinLobbyTask(JoinLobbyMessage message, ManagedReference<PlayerListener> playerRef)
    {
        super(message, playerRef);
    }
    
    public void run()
    {
        PlayerListener playerListener = getPlayerRef().get();
        ClientSession session = playerListener.getSessionRef().get();
        
        //generate the player with the specified attributes
        Player p = PlayerFactory.generatePlayer(
                getMessage().getPlayerType(),
                getMessage().getTeam(),
                session.getName(),
                Constants.DEFAULT_PLAYER_STRENGTH);
        playerListener.setPlayerRef(AppContext.getDataManager().createReference(p));
        
        //add the player to the lobby
        LobbyRoom lobby = (LobbyRoom)AppContext.getDataManager().getBinding(Constants.LOBBY);
        lobby.joinRoom(p, session);
        
        logger.log(Level.INFO, "Player "+p.getUid()+" joined the Lobby");
        ResultSender.sendResult(new SnapshotResult(DataTransformer.generateLobbyRoomDTO(lobby)), session);
   }
}
