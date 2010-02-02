/*
 * SwarmModel.java
 *
 * Created on May 5, 2008, 4:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.sgs.example.swarm.client.model;

import java.util.Observable;

/**
 *
 * @author ok194946
 */
public class SwarmModel extends Observable
{
    private LobbyRoomModel lobbyRoomModel;
    //private GameRoomModel gameRoomModel;
    
    /** Creates a new instance of SwarmModel */
    public SwarmModel()
    {
        lobbyRoomModel = new LobbyRoomModel();
    }
    
    public LobbyRoomModel getLobbyRoomModel() { return lobbyRoomModel; }
    
    public void notifyRoomSwitched()
    {
        //called when a snapshot result is received and therefore 
        //a possible room switch event has occurred
        this.setChanged();
        this.notifyObservers();
    }
}
