/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.sgs.example.swarm.server.tasks;

import java.util.Map;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Iterator;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.TaskManager;
import com.sun.sgs.app.AppContext;

import com.sun.sgs.example.swarm.server.players.PlayerListener;
import com.sun.sgs.example.swarm.shared.messages.AttackMessage;
import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.domain.Room;
import com.sun.sgs.example.swarm.server.domain.GameRoom;
import com.sun.sgs.example.swarm.shared.util.Location;
import com.sun.sgs.example.swarm.server.domain.BoardItem;

/**
 *
 * @author owen
 */
public class AttackTask extends MessageTask<AttackMessage> implements Serializable
{

    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(AttackTask.class.getName());
    
    public AttackTask(AttackMessage message, ManagedReference<PlayerListener> playerRef)
    {
        super(message, playerRef);
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
        
        //determine if attack location is valid
        Location attackLocation = gameRoom.getDirectionLocation(location, getMessage().getDirection());
        if(attackLocation == null)
            return;

        //spawn a new attack task for each item at the attack location
        TaskManager taskManager = AppContext.getTaskManager();
        ManagedReference<GameRoom> gameRef = AppContext.getDataManager().createReference(gameRoom);
        Map<String, ManagedReference<BoardItem>> boardItems = gameRoom.getBoardItems(attackLocation).get();
        for(Iterator<String> ik = boardItems.keySet().iterator(); ik.hasNext(); ) {
            taskManager.scheduleTask(new AttackBoardItemTask(boardItems.get(ik.next()), gameRef, attackLocation, player.getStrength()));
        }
        
        logger.log(Level.INFO, "Attack scheduled for location "+attackLocation.getRow()+", "+attackLocation.getCol()+
                " by player "+player.getUid());
    }
}
