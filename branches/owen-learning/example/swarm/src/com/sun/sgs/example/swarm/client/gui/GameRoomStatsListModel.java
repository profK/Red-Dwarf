package com.sun.sgs.example.swarm.client.gui;

import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractListModel;

import com.sun.sgs.example.swarm.client.model.LobbyRoomModel;
import com.sun.sgs.example.swarm.shared.data.LobbyRoomDTO;

/**
 *
 * @author ok194946
 */
public class GameRoomStatsListModel extends AbstractListModel implements Observer
{
    private LobbyRoomModel lobbyRoomModel;
    
    /** Creates a new instance of GameRoomStatsListModel */
    public GameRoomStatsListModel(LobbyRoomModel lobbyRoomModel)
    {
        this.lobbyRoomModel = lobbyRoomModel;
        lobbyRoomModel.addObserver(this);
    }
    
    public Object getElementAt(int index)
    {
        return lobbyRoomModel.getLobbyRoom().getGameRooms().get(index);
    }
    
    public int getSize()
    {
        LobbyRoomDTO lobby = lobbyRoomModel.getLobbyRoom();
        if(lobby == null) return 0;
        
        return lobbyRoomModel.getLobbyRoom().getGameRooms().size();
    }
    
    public void update(Observable source, Object arg)
    {
        //every update is a full refresh in this case so always fire an update
        this.fireContentsChanged(this, 0, getSize()-1);
    }
    
}
