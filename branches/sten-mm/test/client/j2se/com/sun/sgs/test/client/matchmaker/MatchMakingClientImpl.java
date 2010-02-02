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
package com.sun.sgs.test.client.matchmaker;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.SessionId;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import com.sun.sgs.test.app.matchmaker.common.ByteWrapper;
import com.sun.sgs.test.app.matchmaker.common.CommandList;
import com.sun.sgs.test.app.matchmaker.common.CommandProtocol;

import static com.sun.sgs.test.app.matchmaker.common.CommandProtocol.*;


/**
 * This class is a concrete implementation of the MatchMakingClient. It 
 * communicates with the match making server application via the 
 * SimpleClient for sending commands, and receiving responses.
 */
public class MatchMakingClientImpl implements MatchMakingClient, 
                                                        SimpleClientListener {
	
    private MatchMakingClientListener listener;
    private SimpleClient manager;
    private HashMap<String, LobbyChannelImpl> lobbyMap;
    private HashMap<String, GameChannelImpl> gameMap;

    private CommandProtocol protocol;

    /**
     * Constructs a new MatchMakingClient.
     * 
     * @param manager the ClientConnectionManager used for server
     * commmunication
     */
    public MatchMakingClientImpl() {
        protocol = new CommandProtocol();
        lobbyMap = new HashMap<String, LobbyChannelImpl>();
        gameMap = new HashMap<String, GameChannelImpl>();
    }
    
    public void setSimpleClient(SimpleClient manager) {
        this.manager = manager;
    }

    /**
     * Sends a command to the server via the ClientConnectionManager.
     * 
     * @param list	the list containing the components of the request packet
     */
    void sendCommand(CommandList list) throws IOException {
    	manager.send(list.getByteArray());
    }

    public void setListener(MatchMakingClientListener listener) {
        this.listener = listener;
    }

    public void listFolder(String folderName) throws IOException {
        CommandList list = new CommandList(LIST_FOLDER_REQUEST);
        if (folderName != null) {
            list.add(folderName);
        }

        sendCommand(list);
    }

    public void joinLobby(String name, String password) throws IOException {
        CommandList list = new CommandList(JOIN_LOBBY);
        list.add(name);
        if (password != null) {
            list.add(password);
        }

        sendCommand(list);
    }

    public void joinGame(String gameName) throws IOException {
        joinGame(gameName, null);
    }

    public void joinGame(String gameName, String password) throws IOException {
        CommandList list = new CommandList(JOIN_GAME);
        list.add(gameName);
        if (password != null) {
            list.add(password);
        }

        sendCommand(list);
    }
    
    public void leaveLobby() throws IOException {
    	sendCommand(new CommandList(LEAVE_LOBBY));
    }
    
    public void leaveGame() throws IOException {
    	sendCommand(new CommandList(LEAVE_GAME));
    }
    
    public void completeGame(String gameName) throws IOException {
    	CommandList list = new CommandList(GAME_COMPLETED);
    	list.add(gameName);
    	
    	sendCommand(list);
    }
    
    public void reconnected() {
        // TODO not implemented
    }


    /**
     * {@inheritDoc}
     */
    public ClientChannelListener joinedChannel(ClientChannel channel) {
        if (channel.getName().indexOf(":") == -1) { // lobby
            LobbyChannelImpl lobby = new LobbyChannelImpl(channel, this);
            lobbyMap.put(channel.getName(), lobby);
            listener.joinedLobby(lobby);
            return lobby;
        } 
        else { // game
            GameChannelImpl game = new GameChannelImpl(channel, this);
            gameMap.put(channel.getName(), game);
            
            listener.joinedGame(game);
            return game;
        }

    }

    /**
     * Called to parse out the ListFolderResponse response from the
     * server.
     * 
     * @param data the data buffer containing the requested folder and
     * lobby detail.
     */
    private void listFolderResponse(ByteBuffer data) {
        String folderName = protocol.readString(data);
        int numFolders = data.getInt();
        FolderDescriptor[] subfolders = new FolderDescriptor[numFolders];
        for (int i = 0; i < numFolders; i++) {
            String curFolderName = protocol.readString(data);
            String curFolderDescription = protocol.readString(data);
            subfolders[i] = new FolderDescriptor(curFolderName, 
                                                        curFolderDescription);
        }
        int numLobbies = data.getInt();
        LobbyDescriptor[] lobbies = new LobbyDescriptor[numLobbies];
        for (int i = 0; i < numLobbies; i++) {
            String curLobbyName = protocol.readString(data);
            String curLobbyChannelName = protocol.readString(data);
            String curLobbyDescription = protocol.readString(data);
            int numUsers = data.getInt();
            int maxUsers = data.getInt();
            boolean isPasswordProtected = protocol.readBoolean(data);

            lobbies[i] = new LobbyDescriptor(curLobbyName,
                    curLobbyChannelName, curLobbyDescription, numUsers,
                    maxUsers, isPasswordProtected);
        }
        listener.listedFolder(folderName, subfolders, lobbies);
    }


    private void gameParametersResponse(ByteBuffer data) {
        String lobbyName = protocol.readString(data);
        int numParams = data.getInt();
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        for (int i = 0; i < numParams; i++) {
            String param = protocol.readString(data);
            ByteWrapper value = protocol.readParamValue(data);
            paramMap.put(param, value.getValue());
        }
        LobbyChannelImpl lobby = lobbyMap.get(lobbyName);
        if (lobby != null) {
            lobby.receiveGameParameters(paramMap);
        }
    }

    private void createGameFailed(ByteBuffer data) {
        String game = protocol.readString(data);
        int errorCode = protocol.readUnsignedByte(data);
        String lobbyName = protocol.readString(data);
        if (lobbyName == null) {
            return;
        }
        LobbyChannelImpl lobby = lobbyMap.get(lobbyName);
        if (lobby != null) {
            lobby.createGameFailed(game, errorCode);
        }
    }

    public void loggedIn() {
        listener.connected();
    }

    public void loginFailed(String reason) {
        listener.connectionFailed(reason);
    }

    public void receivedMessage(byte[] message) {
        ByteBuffer data = ByteBuffer.allocate(message.length);
        data.put(message);
        data.flip();
        int command = protocol.readUnsignedByte(data);
        if (command == LIST_FOLDER_RESPONSE) {
            listFolderResponse(data);
        } 
        else if (command == GAME_PARAMETERS_RESPONSE) {
            gameParametersResponse(data);
        } 
        else if (command == CREATE_GAME_FAILED) {
            createGameFailed(data);
        } 
        else if (command == ERROR) {
            listener.error(protocol.readUnsignedByte(data));
        }        
    }

    public void reconnecting() {
        // TODO -- not implemented...
        
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return listener.getPasswordAuthentication();
    }

    public void disconnected(boolean graceful, String reason) {
        listener.disconnected();
    }
}
