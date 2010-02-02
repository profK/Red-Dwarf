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

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.test.app.matchmaker.common.ByteWrapper;

/**
 * Represents a Lobby in the Match Making application.  Lobbies function both 
 * as a starting point for creating games and as a chat area.  Lobbies are 
 * loosely mapped to channels. A lobby channel name is in the form of: 
 * FolderName.SubFolderName.LobbyName.
 */
public class Lobby extends ChannelRoom {

    private static final long serialVersionUID = 1L;

    private int maxPlayers;
    private boolean canHostBoot;
    private boolean canHostBan;
    private boolean canHostChangeSettings;
    private int maxConnectionTime;
    private int maxPlayersInGameRoom = 0;
    private int minPlayersInGameRoomStart = 0;
    private int maxPlayersInGameRoomStart = 0;
    private HashMap<String, ByteWrapper> gameParameters;
    private List<ManagedReference> gameRoomList;
    private List<ClientSession> playerList;

    public Lobby(String name, String description, String password,
            String channelName) {
        super(name, description, password, channelName);

        gameParameters = new HashMap<String, ByteWrapper>();
        gameRoomList = new LinkedList<ManagedReference>();
        this.playerList = new LinkedList<ClientSession>();
    }

    public void setMaxPlayers(int num) {
        maxPlayers = num;
    }
    
    public void setMaxPlayersInGameRoom(int num) {
    	maxPlayersInGameRoom = num;
    }
    
    public int getMaxPlayersInGameRoom() {
    	return maxPlayersInGameRoom;
    }
    
    public void setMinPlayersInGameRoomStart(int num) {
    	minPlayersInGameRoomStart = num;
    }
    
    public int getMinPlayersInGameRoomStart() {
    	return minPlayersInGameRoomStart;
    }
    
    public void setMaxPlayersInGameRoomStart(int num) {
    	maxPlayersInGameRoomStart = num;
    }
    
    public int getMaxPlayersInGameRoomStart() {
    	return maxPlayersInGameRoomStart;
    }

    public void setCanHostBoot(boolean b) {
        canHostBoot = b;
    }

    public void setCanHostBan(boolean b) {
        canHostBan = b;
    }

    public void setCanHostChangeSettings(boolean b) {
        canHostChangeSettings = b;
    }

    public void setMaxConnectionTime(int time) {
        maxConnectionTime = time;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean getCanHostBoot() {
        return canHostBoot;
    }

    public boolean getCanHostBan() {
        return canHostBan;
    }

    public boolean getCanHostChangeSettings() {
        return canHostChangeSettings;
    }

    public int getMaxConnectionTime() {
        return maxConnectionTime;
    }

    public void addGameRoom(ManagedReference grRef) {
        gameRoomList.add(grRef);
    }
    
    public List<ManagedReference> getGameRoomList() {
    	return Collections.unmodifiableList(gameRoomList);
    }

    public void addGameParameter(String key, ByteWrapper value) {
        gameParameters.put(key, value);
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
        if (!playerList.contains(user)) {
            playerList.add(user);
        }
    }

    public void removeUser(ClientSession user) {
        playerList.remove(user);
    }
    
    public void removeGameRoom(ManagedReference grRef) {
    	gameRoomList.remove(grRef);
    }

    public List<ClientSession> getUsers() {
        return playerList;
    }

    public void removeAllUsers() {
        playerList.clear();
    }

    public int getNumPlayers() {
        return playerList.size();
    }
}
