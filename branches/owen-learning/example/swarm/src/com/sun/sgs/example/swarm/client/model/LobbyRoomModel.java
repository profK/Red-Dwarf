/*
 * LobbyRoomModel.java
 *
 * Created on May 5, 2008, 11:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.sgs.example.swarm.client.model;

import java.util.Observable;

import com.sun.sgs.example.swarm.shared.data.LobbyRoomDTO;

/**
 *
 * @author ok194946
 */
public class LobbyRoomModel extends Observable
{
    private LobbyRoomDTO lobby;
    
    /** Creates a new instance of LobbyRoomModel */
    public LobbyRoomModel()
    {
        this.lobby = null;
    }
    
    public LobbyRoomDTO getLobbyRoom() { return lobby; }
    public void setLobbyRoom(LobbyRoomDTO lobby)
    {
        this.lobby = lobby;
        setChanged();
        notifyObservers();
    }
}
