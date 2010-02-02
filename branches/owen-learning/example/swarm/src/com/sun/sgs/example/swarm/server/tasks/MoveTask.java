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

import com.sun.sgs.example.swarm.shared.messages.MoveMessage;
import com.sun.sgs.example.swarm.server.players.PlayerListener;
import com.sun.sgs.example.swarm.server.domain.Room;
import com.sun.sgs.example.swarm.server.domain.GameRoom;
import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.domain.BoardItem;
import com.sun.sgs.example.swarm.shared.util.Location;
import com.sun.sgs.example.swarm.server.factories.ResultSender;
import com.sun.sgs.example.swarm.shared.results.PlayerMovedResult;

/**
 *
 * @author owen
 */
public class MoveTask extends MessageTask<MoveMessage> implements Serializable
{

    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(MoveTask.class.getName());
    
    public MoveTask(MoveMessage message, ManagedReference<PlayerListener> player)
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
        
        //determine if move location is valid
        Location moveLocation = gameRoom.getDirectionLocation(location, getMessage().getDirection());
        if(moveLocation == null)
            return;
        
        //move the player to the new location
        Map<String, ManagedReference<BoardItem>> oldBoardItems = gameRoom.getBoardItems(location).getForUpdate();
        Map<String, ManagedReference<BoardItem>> newBoardItems = gameRoom.getBoardItems(moveLocation).getForUpdate();
        ManagedReference<BoardItem> playerItem = oldBoardItems.get(player.getUid());
        oldBoardItems.remove(player.getUid());
        newBoardItems.put(player.getUid(), playerItem);

        logger.log(Level.INFO, "Player "+player.getUid()+"moved to location "+moveLocation.getRow()+", "+moveLocation.getCol());
        ResultSender.sendResult(new PlayerMovedResult(player.getUid(), moveLocation), gameRoom.getPlayerChannel());
    }
}
