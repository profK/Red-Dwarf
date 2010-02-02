package com.sun.sgs.example.swarm.server.tasks;

import java.util.Map;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Iterator;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.TaskManager;
import com.sun.sgs.app.AppContext;

import com.sun.sgs.example.swarm.shared.messages.BuildMessage;
import com.sun.sgs.example.swarm.server.players.PlayerListener;
import com.sun.sgs.example.swarm.server.domain.Room;
import com.sun.sgs.example.swarm.server.domain.GameRoom;
import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.domain.BoardItem;
import com.sun.sgs.example.swarm.server.domain.Building;
import com.sun.sgs.example.swarm.shared.util.Location;
import com.sun.sgs.example.swarm.server.Constants;

/**
 *
 * @author owen
 */
public class BuildTask extends MessageTask<BuildMessage> implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(BuildTask.class.getName());
    
    public BuildTask(BuildMessage message, ManagedReference<PlayerListener> player)
    {
        super(message, player);
    }
    
    public void run()
    {
        PlayerListener playerListener = getPlayerRef().get();
        
        //verify that player is in a game room
        GameRoom gameRoom = null;
        Room room = playerListener.getRoomRef().get();
        if(room instanceof GameRoom)
            gameRoom = (GameRoom)room;
        else return;
        
        //determine player's location
        Player player = playerListener.getPlayerRef().get();
        Location location = gameRoom.getPlayerLocation(player);
        
        //determine if build location is valid
        Location buildLocation = gameRoom.getDirectionLocation(location, getMessage().getDirection());
        if(buildLocation == null)
            return;
        
        //build a new building if there is none at the build location
        BoardItem newBuilding = null;
        Map<String, ManagedReference<BoardItem>> boardItems = gameRoom.getBoardItems(buildLocation).get();
        if(!boardItems.containsKey(Constants.BUILDING_KEY)) {
            AppContext.getDataManager().markForUpdate(boardItems);
            newBuilding = new Building(player.getStrength(), player.getTeam());
        }

        //spawn a new attack task for each item at the build location
        TaskManager taskManager = AppContext.getTaskManager();
        ManagedReference<GameRoom> gameRef = AppContext.getDataManager().createReference(gameRoom);
        for(Iterator<String> ik = boardItems.keySet().iterator(); ik.hasNext(); ) {
            taskManager.scheduleTask(new RepairBoardItemTask(boardItems.get(ik.next()), gameRef, buildLocation, player.getStrength()));
        }
        
        //add the new building if it was built
        if(newBuilding != null) {
            boardItems.put(Constants.BUILDING_KEY, AppContext.getDataManager().createReference(newBuilding));
        }
        
        logger.log(Level.INFO, "Repair scheduled for location "+buildLocation.getRow()+", "+buildLocation.getCol()+
                " by player "+player.getUid());
    }
}
