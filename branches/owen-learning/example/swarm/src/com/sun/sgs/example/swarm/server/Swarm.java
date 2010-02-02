package com.sun.sgs.example.swarm.server;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashMap;

import com.sun.sgs.example.swarm.server.domain.Room;
import com.sun.sgs.example.swarm.server.domain.LobbyRoom;
import com.sun.sgs.example.swarm.server.players.PlayerListener;

/**
 * This is the main AppListener for the Swarm app
 *
 */
public class Swarm implements AppListener, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(Swarm.class.getName());
    
    /** The lobby of the swarm world */
    private ManagedReference<LobbyRoom> lobbyRef;
    /** The map of user ids to rooms of logged in users */
    private ManagedReference<Map<String, Room>> userIdsRef;

    
    /**
     * Initialize the Swarm world.
     * The world consists of a lobby with a given number of game rooms.
     */
    public void initialize(Properties props)
    {
        logger.log(Level.INFO, "Swarm World booting up...");
        
        //parse properties
        Integer maxTotalPlayers = Integer.valueOf(props.getProperty("com.sun.sgs.example.swarm.server.maxTotalPlayers"));
        Integer totalRooms = Integer.valueOf(props.getProperty("com.sun.sgs.example.swarm.server.totalRooms"));
        Integer maxRoomPlayers = Integer.valueOf(props.getProperty("com.sun.sgs.example.swarm.server.maxRoomPlayers"));
        Integer roomRows = Integer.valueOf(props.getProperty("com.sun.sgs.example.swarm.server.roomRows"));
        Integer roomCols = Integer.valueOf(props.getProperty("com.sun.sgs.example.swarm.server.roomCols"));
        
        DataManager dm = AppContext.getDataManager();
        
        //create lobby
        LobbyRoom lobby = new LobbyRoom(maxTotalPlayers.intValue(), totalRooms.intValue(), maxRoomPlayers.intValue(), roomRows.intValue(), roomCols.intValue());
        lobbyRef = dm.createReference(lobby);
        dm.setBinding(Constants.LOBBY, lobby);
        logger.log(Level.INFO, " Lobby created:");
        logger.log(Level.INFO, "  Maximum Total Players: "+maxTotalPlayers.toString());
        logger.log(Level.INFO, "  Total Rooms Available: "+totalRooms.toString());
        logger.log(Level.INFO, "  Maximum Players/Room : "+maxRoomPlayers.toString());
        logger.log(Level.INFO, "  Room Size            : "+roomRows.toString()+" x "+roomCols.toString());
        
        //create set of logged in user ids
        Map<String, Room> users = new ScalableHashMap<String, Room>();
        userIdsRef = dm.createReference(users);
        dm.setBinding(Constants.USER_LIST, users);
        logger.log(Level.INFO, " Logged In user map created.");
        
        logger.log(Level.INFO, "Swarm World booted up successfully!");
        
    }
    
    public ClientSessionListener loggedIn(ClientSession session)
    {
        //check if the user is already logged in
        if(userIdsRef.get().keySet().contains(session.getName()))
            return null;
        
        //add the user to the user set (initial room is null)
        userIdsRef.getForUpdate().put(session.getName(), null);
        
        //return a PlayerListener
        return new PlayerListener(session);
    }
    
}
