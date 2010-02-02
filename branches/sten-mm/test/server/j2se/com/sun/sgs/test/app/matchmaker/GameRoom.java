/*
 Copyright (c) 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa
 Clara, California 95054, U.S.A. All rights reserved.
 
 Sun Microsystems, Inc. has intellectual property rights relating to
 technology embodied in the product that is described in this document.
 In particular, and without limitation, these intellectual property rights
 may include one or more of the U.S. patents listed at
 http://www.sun.com/patents and one or more additional patents or pending
 patent applications in the U.S. and in other countries.
 
 U.S. Government Rights - Commercial software. Government users are subject
 to the Sun Microsystems, Inc. standard license agreement and applicable
 provisions of the FAR and its supplements.
 
 This distribution may include materials developed by third parties.
 
 Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
 trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 
 UNIX is a registered trademark in the U.S. and other countries, exclusively
 licensed through X/Open Company, Ltd.
 
 Products covered by and information contained in this service manual are
 controlled by U.S. Export Control laws and may be subject to the export
 or import laws in other countries. Nuclear, missile, chemical biological
 weapons or nuclear maritime end uses or end users, whether direct or
 indirect, are strictly prohibited. Export or reexport to countries subject
 to U.S. embargo or to entities identified on U.S. export exclusion lists,
 including, but not limited to, the denied persons and specially designated
 nationals lists is strictly prohibited.
 
 DOCUMENTATION IS PROVIDED "AS IS" AND ALL EXPRESS OR IMPLIED CONDITIONS,
 REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 ARE DISCLAIMED, EXCEPT TO THE EXTENT THAT SUCH DISCLAIMERS ARE HELD TO BE
 LEGALLY INVALID.
 
 Copyright © 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 California 95054, Etats-Unis. Tous droits réservés.
 
 Sun Microsystems, Inc. détient les droits de propriété intellectuels
 relatifs à la technologie incorporée dans le produit qui est décrit dans
 ce document. En particulier, et ce sans limitation, ces droits de
 propriété intellectuelle peuvent inclure un ou plus des brevets américains
 listés à l'adresse http://www.sun.com/patents et un ou les brevets
 supplémentaires ou les applications de brevet en attente aux Etats -
 Unis et dans les autres pays.
 
 Cette distribution peut comprendre des composants développés par des
 tierces parties.
 
 Sun, Sun Microsystems, le logo Sun et Java sont des marques de fabrique
 ou des marques déposées de Sun Microsystems, Inc. aux Etats-Unis et dans
 d'autres pays.
 
 UNIX est une marque déposée aux Etats-Unis et dans d'autres pays et
 licenciée exlusivement par X/Open Company, Ltd.
 
 see above Les produits qui font l'objet de ce manuel d'entretien et les
 informations qu'il contient sont regis par la legislation americaine en
 matiere de controle des exportations et peuvent etre soumis au droit
 d'autres pays dans le domaine des exportations et importations.
 Les utilisations finales, ou utilisateurs finaux, pour des armes
 nucleaires, des missiles, des armes biologiques et chimiques ou du
 nucleaire maritime, directement ou indirectement, sont strictement
 interdites. Les exportations ou reexportations vers des pays sous embargo
 des Etats-Unis, ou vers des entites figurant sur les listes d'exclusion
 d'exportation americaines, y compris, mais de maniere non exclusive, la
 liste de personnes qui font objet d'un ordre de ne pas participer, d'une
 facon directe ou indirecte, aux exportations des produits ou des services
 qui sont regi par la legislation americaine en matiere de controle des
 exportations et la liste de ressortissants specifiquement designes, sont
 rigoureusement interdites.
 
 LA DOCUMENTATION EST FOURNIE "EN L'ETAT" ET TOUTES AUTRES CONDITIONS,
 DECLARATIONS ET GARANTIES EXPRESSES OU TACITES SONT FORMELLEMENT EXCLUES,
 DANS LA MESURE AUTORISEE PAR LA LOI APPLICABLE, Y COMPRIS NOTAMMENT TOUTE
 GARANTIE IMPLICITE RELATIVE A LA QUALITE MARCHANDE, A L'APTITUDE A UNE
 UTILISATION PARTICULIERE OU A L'ABSENCE DE CONTREFACON.
*/
package com.sun.sgs.test.app.matchmaker;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.test.app.matchmaker.common.ByteWrapper;

/**
 * Represents a Game Room in the match making application.  A GameRoom Channel 
 * name is in the form of FolderName.SubFolderName.LobbyName:GameRoomName. 
 */
public class GameRoom extends ChannelRoom {

    private static final long serialVersionUID = 1L;

    private ClientSession host;
    private int maxPlayers;
    private HashMap<String, ByteWrapper> gameParameters;
    
    /**  users mapped to ready state. */
    private HashMap<ClientSession, Boolean> readyMap; 
    
    /** Username to ClientSession map */
    private HashMap<String, ClientSession> userMap;
    
    /** List of banned users. */
    private List<String> bannedList;
    private boolean started = false;
    private ManagedReference parentLobby;	// a reference to the lobby to
    						// which this game room belongs

    public GameRoom(String name, String description, String password,
            String channelName, ClientSession host, ManagedReference lobby) {
    	
        super(name, description, password, channelName);

        userMap = new HashMap<String, ClientSession>();
        readyMap = new HashMap<ClientSession, Boolean>();

        this.host = host;
        this.parentLobby = lobby;
        gameParameters = new HashMap<String, ByteWrapper>();
        bannedList = new LinkedList<String>();
    }
    
    public ClientSession getHost() {
        return host;
    }

    public void addGameParameter(String key, ByteWrapper value) {
        gameParameters.put(key, value);
    }
    
    /**
     * Returns true if the requested key from the given Entry is updated with 
     * the given value. False will be returned if the key does not exist, 
     * or if the values are not different.  
     * 
     * @param entry	an entry to update, the key must exist and the values
     *                  must be different
     * 
     * @return true if the parameter update was successful.
     */
    public boolean updateGameParameter(Entry<String, ByteWrapper> entry) {
    	
    	if (!gameParameters.containsKey(entry.getKey())) {
    	    return false;
    	}
    	if (gameParameters.get(entry.getKey()).equals(entry.getValue())) {
    	    return false;
    	}
    	gameParameters.put(entry.getKey(), entry.getValue());
    	return true;
    }
    
    public boolean hasStarted() {
    	return started;
    }
    
    public void setStarted(boolean b) {
    	started = b;
    }
    
    public ManagedReference getLobby() {
    	return parentLobby;
    }
    
    public void setMaxPlayers(int num) {
    	maxPlayers = num;
    }
   
    public int getMaxPlayers() {
    	return maxPlayers;
    }
    
    public ClientSession getSession(String player) {
        return userMap.get(player);
    }
    
    /**
     * Attempts to boot (and optionally ban) the given player from the game.
     * 
     * @param player		the player to boot/ban
     * @param shouldBan		if true, will add the player to the "banned" 
     *                          list
     * 
     * @return true if the boot/ban was successful, false if the
     * 			player was not found.
     */
    public boolean bootPlayer(String player, boolean shouldBan) {
    	if (!userMap.containsKey(player)) {
    	    return false;
    	}
    	ClientSession session = userMap.remove(player);
        readyMap.remove(session);
    	if (shouldBan && !bannedList.contains(player)) {
    	    bannedList.add(player);
    	}
    	return true;
    }
    
    /**
     * Returns true if the given player has been banned from this game.
     * 
     * @param player			the player
     * 
     * @return true if this player is banned
     */
    public boolean isPlayerBanned(ClientSession player) {
    	return bannedList.contains(player.getName());
    }

    /**
     * Returns a read-only view of the game parameters map.
     * 
     * @return a read-only view of the game parameters map.
     */
    public Map<String, ByteWrapper> getGameParamters() {
        return Collections.unmodifiableMap(gameParameters);
    }

    public void addUser(ClientSession user) {
        if (!userMap.containsKey(user.getName())) {
            userMap.put(user.getName(), user);
            readyMap.put(user, false);
        }
    }

    /**
     * Updates the "ready state" of the given user for this game.
     * 
     * @param user the user to update
     * @param ready if true, the user is ready
     */
    public void updateReady(ClientSession user, boolean ready) {
        readyMap.put(user, ready);
    }

    public void removeUser(ClientSession user) {
        userMap.remove(user.getName());
        readyMap.remove(user);
    }

    public List<ClientSession> getUsers() {
        List<ClientSession> playerList = 
                            new LinkedList<ClientSession>(readyMap.keySet());

        return playerList;
    }

    public void removeAllUsers() {
        userMap.clear();
        readyMap.clear();
    }

    public int getNumPlayers() {
        return userMap.size();
    }
    
    public boolean isPlayerReady(ClientSession userID) {
    	return readyMap.get(userID);
    }

    /**
     * Returns true if all joined players have indicated that they are
     * ready.
     * 
     * @return true if all players are ready for game start.
     */
    public boolean arePlayersReady() {
        return !readyMap.containsValue(false);
    }

}
