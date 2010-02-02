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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sun.sgs.test.app.matchmaker.common.CommandProtocol.*;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.util.LoggerWrapper;
import com.sun.sgs.test.app.matchmaker.common.ByteWrapper;
import com.sun.sgs.test.app.matchmaker.common.CommandList;
import com.sun.sgs.test.app.matchmaker.common.CommandProtocol;
import com.sun.sgs.test.app.matchmaker.common.UnsignedByte;

/**
 * This class represents a Player that is currently
 * on-line. The Player object acts as a "command proxy" for the
 * associated user. Commands arrive via the messageReceived callback. The 
 * command is processed and a response sent back to the user.
 * 
 */
public class Player implements ClientSessionListener, ManagedObject, 
                                                            Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(Player.class.getName()));    

    private ClientSession clientSession;
    private ManagedReference folderRoot;
    private ManagedReference currentLobby;
    private ManagedReference currentGameRoom;

    private CommandProtocol protocol;

    public Player(ClientSession session, ManagedReference root) {
        this.clientSession = session;
        this.folderRoot = root;

        protocol = new CommandProtocol();
    }

    public ManagedReference getCurrentLobby() {
        return currentLobby;
    }
    
    public ClientSession getClientSession() {
        return clientSession;
    }

    /**
     * Called when a message is received by the associated client.  The message
     * is interpreted as a command in the well-known match maker protocol.
     * 
     * @param message           the received message as bytes
     */
    public void receivedMessage(byte[] message) {
        ByteBuffer data = ByteBuffer.allocate(message.length);
        data.put(message);
        data.flip();
        int commandCode = protocol.readUnsignedByte(data);
        logger.log(Level.FINE, "Player.receivedMessage: 0x" + 
                                        Integer.toHexString(commandCode));
        if (commandCode == LIST_FOLDER_REQUEST) {
            listFolderRequest(data);
        } 
        else if (commandCode == JOIN_LOBBY) {
            joinLobby(data);
        } 
        else if (commandCode == JOIN_GAME) {
            joinGame(data);
        } 
        else if (commandCode == GAME_PARAMETERS_REQUEST) {
            gameParametersRequest();
        } 
        else if (commandCode == CREATE_GAME) {
            createGame(data);
        } 
        else if (commandCode == UPDATE_PLAYER_READY_REQUEST) {
            updatePlayerReadyRequest(data);
        } 
        else if (commandCode == START_GAME_REQUEST) {
            startGame();
        } 
        else if (commandCode == LEAVE_LOBBY) {
            leaveLobby();
        } 
        else if (commandCode == LEAVE_GAME) {
            leaveGame();
        } 
        else if (commandCode == GAME_COMPLETED) {
            gameCompleted(data);
        } 
        else if (commandCode == BOOT_REQUEST) {
            bootPlayer(data);
        } 
        else if (commandCode == UPDATE_GAME_REQUEST) {
            updateGameRequest(data);
        }
       
    }

    /**
     * Once a game is started, it stays around until the GAME_COMPLETED 
     * command is received from the host.  Once this command is received,
     * the users are unjoined from the channel (which implicitly closes it).
     * 
     * @param data			the buffer containing the game ID
     */
    private void gameCompleted(ByteBuffer data) {
        String gameName = protocol.readString(data);
        ManagedMap<String, ManagedReference> gameRoomMap =
            AppContext.getDataManager().getBinding("GameRoomMap", 
                                                            ManagedMap.class);
        ManagedReference gameRef = gameRoomMap.get(gameName);
        if (gameRef == null) {
            sendErrorResponse(INVALID_GAME);
            return;
        }
        GameRoom gameRoom = gameRef.getForUpdate(GameRoom.class);
        if (!gameRoom.getHost().equals(clientSession)) {
            sendErrorResponse(PLAYER_NOT_HOST);
            return;
        }
        cleanupGame(gameRoom);
    }

    /**
     * Send a "catch-up" join message to the user for every player already on
     * the given channel.
     * 
     * @param users		the list of users
     * @param commandCode	the type of entry (lobby or game room)
     * @param channel		the channel on which to send the message
     */
    private void sendRetroJoins(List<ClientSession> users, int commandCode, 
                                                            Channel channel) {
        for (ClientSession curUser : users) {
            if (curUser.equals(clientSession)) {
                continue;
            }
            
            CommandList list = new CommandList(commandCode);
            list.add(curUser.getSessionId());
            list.add(curUser.getName());
            
            sendResponse(list, channel);
    	}
    	
    }

    /**
     * Unjoins remaining users from the game and removes it
     * from the game map.
     * 
     * @param gameRoom		the GameRoom, should have GET access
     */
    private void cleanupGame(GameRoom gameRoom) {
        Channel gameChannel = 
           AppContext.getChannelManager().getChannel(gameRoom.getChannelName());
        for (ClientSession curSession : gameRoom.getUsers()) {
            gameChannel.leave(curSession);
            
            leftGame(gameRoom, curSession);
        }
        ManagedMap<String, ManagedReference> gameRoomMap =
            AppContext.getDataManager().getBinding("GameRoomMap", 
                                                            ManagedMap.class); 
        AppContext.getDataManager().markForUpdate(gameRoomMap);
        gameRoomMap.remove(gameRoom.getName());
    }

    /**
     * Responds to the ListFolderRequest command protocol. Reads the
     * requested FolderID off the buffer and attempts to find the folder
     * with the matching ID. If found, the subfolders of this folder are
     * listed as well as any lobbies.
     * 
     * @param task
     * @param data
     */
    
    private void listFolderRequest(ByteBuffer data) {
        String folderID = null;
        if (data.hasRemaining()) {
            folderID = protocol.readString(data);
        }
        Folder root = folderRoot.get(Folder.class);
        Folder targetFolder = folderID == null ? root : root.findFolder(folderID);
        CommandList list = new CommandList(LIST_FOLDER_RESPONSE);
        list.add(folderID == null ? root.getName() : folderID);
        if (targetFolder != null) {
            list.add(targetFolder.getFolders().size());
            for (ManagedReference folderRef : targetFolder.getFolders()) {
                Folder curFolder = folderRef.get(Folder.class);

                // list out the contents of the current folder
                list.add(curFolder.getName());
                list.add(curFolder.getDescription());

            }

            // finally list out the lobbies.
            List<ManagedReference> lobbyList = targetFolder.getLobbies();
            list.add(lobbyList.size());
            for (ManagedReference lobbyRef : lobbyList) {
                Lobby curLobby = lobbyRef.get(Lobby.class);

                list.add(curLobby.getName());
                list.add(curLobby.getChannelName());
                list.add(curLobby.getDescription());
                list.add(curLobby.getNumPlayers());
                list.add(curLobby.getMaxPlayers());
                list.add(curLobby.isPasswordProtected());
            }
        } else { // return a zero size
            list.add(0);
        }
        sendResponse(list);
    }
    
    private void leaveLobby() {
    	if (!checkLobby()) {
    	    return;
    	}
    	Lobby lobby = currentLobby.get(Lobby.class);
        Channel channel = 
            AppContext.getChannelManager().getChannel(lobby.getChannelName());
    	channel.leave(clientSession);
        currentLobby = null;
        
        if (currentGameRoom != null) {
            GameRoom gr = currentGameRoom.get(GameRoom.class);
            if (!gr.hasStarted()) {
                doLeaveGame(gr, clientSession);
            }
        }  
  
        leftLobby(lobby, clientSession);
    }
    
    private void leftLobby(Lobby lobby, ClientSession session) {
        lobby.removeUser(session);
        
        // send PlayerLeftLobby message to lobby
        CommandList playerLeftList = new CommandList(PLAYER_LEFT_LOBBY);
        playerLeftList.add(session.getSessionId());

        sendMulticastResponse(lobby.getUsers(), playerLeftList, 
            AppContext.getChannelManager().getChannel(lobby.getChannelName()));
        
    }    
    
    private void leaveGame() {
    	if (!checkGameRoom()) {
    	    return;
    	}
        
        GameRoom gr = currentGameRoom.getForUpdate(GameRoom.class);
        doLeaveGame(gr, clientSession);
    }
    
    private void doLeaveGame(GameRoom gameRoom, ClientSession session) {
        Channel grChannel = 
           AppContext.getChannelManager().getChannel(gameRoom.getChannelName());
        grChannel.leave(session);
        
        gameRoom.removeUser(session);

        // if this was the host, kick everyone out and kill the game
        if (gameRoom.getHost().equals(session)) {
            cleanupGame(gameRoom);
        }
        Player p = AppContext.getDataManager().getBinding(session.getName(), 
                                                            Player.class);

        
        if (gameRoom.hasStarted()) {
            return;
        }
        leftGame(gameRoom, session);
    }
    
    protected void resetGameRoom() {
        currentGameRoom = null;
    }
    
    private void leftGame(GameRoom gameRoom, ClientSession session) {

        Lobby lobby = gameRoom.getLobby().getForUpdate(Lobby.class);
        // send PlayerLeftGame message to lobby and game room
        CommandList playerLeftList = new CommandList(PLAYER_LEFT_GAME);
        playerLeftList.add(session.getSessionId());
        playerLeftList.add(gameRoom.getName());

        
        sendMulticastResponse(lobby.getUsers(), playerLeftList, 
                        AppContext.getChannelManager().
                            getChannel(lobby.getChannelName()));
        
        Channel gameChannel = AppContext.getChannelManager().
                                    getChannel(gameRoom.getChannelName());
        sendMulticastResponse(gameRoom.getUsers(), playerLeftList, gameChannel);        
        
        // If this is the last person leaving the game room, 
        // send notification to the Lobby that the game was killed
        // and remove the game from the lobby list
        if (gameRoom.getHost().equals(session)) {
            lobby.removeGameRoom(currentGameRoom);
                
            CommandList list = new CommandList(GAME_DELETED);
            packGameDescriptor(list, gameRoom);

            sendMulticastResponse(lobby.getUsers(), list, 
                AppContext.getChannelManager().getChannel(
                                                    lobby.getChannelName()));
            
            gameChannel.close();
        }
        
        Player p = AppContext.getDataManager().getBinding(session.getName(), 
                                                                Player.class);
        
        p.resetGameRoom();
        
    }
    
    private void updateGameRequest(ByteBuffer data) {
    	if (!checkLobby()) {
    	    return;
    	}
    	if (!checkGameRoom()) {
    	    return;
    	}
    	
    	String gameName = protocol.readString(data);
    	String gameDescription = protocol.readString(data);
    	String password = protocol.readString(data);
    	HashMap<String, ByteWrapper> gameParameters = readGameParameters(data);
    	
    	GameRoom gameRoom = currentGameRoom.getForUpdate(GameRoom.class);
    	if (gameName != null) {
            if (!filterGameName(gameName)) {
                sendGameUpdateFailedResponse(gameName, gameDescription,  
            			gameParameters, INVALID_GAME_NAME, 
                                gameRoom.getChannelName());
            	return;
            }
            gameRoom.setName(gameName);
    	}
    	if (gameDescription != null) {
    	    gameRoom.setDescription(gameDescription);
    	}
   	gameRoom.setPassword(password);
    	
        for (Entry<String, ByteWrapper> curEntry : gameParameters.entrySet()) {
            gameRoom.updateGameParameter(curEntry);
    	}
    	
    	CommandList list = new CommandList(GAME_UPDATED);
    	packGameDescriptor(list, gameRoom);
    	
        Channel grChannel = AppContext.getChannelManager().getChannel(
                                                    gameRoom.getChannelName());
    	sendMulticastResponse(gameRoom.getUsers(), list, grChannel);
    	
    	Lobby lobby = currentLobby.get(Lobby.class);
        Channel lobbyChannel = AppContext.getChannelManager().getChannel(
                                                    lobby.getChannelName());
    	sendMulticastResponse(lobby.getUsers(), list, lobbyChannel);
    }
    
    private void bootPlayer(ByteBuffer data) {
    	String bootee = protocol.readString(data);
        boolean shouldBan = protocol.readBoolean(data);
    	if (!checkGameRoom()) {
    	    return;
    	}
    	GameRoom gameRoom = currentGameRoom.getForUpdate(GameRoom.class);
    	Channel gameChannel = AppContext.getChannelManager().getChannel(
                                                    gameRoom.getChannelName());
        if (currentLobby == null) {
    	    sendBootFailedResponse(bootee, shouldBan, NOT_CONNECTED_LOBBY, gameChannel);
    	    return;
    	}
    	Lobby lobby = currentLobby.get(Lobby.class);
    	if (!lobby.getCanHostBoot()) {
    	    sendBootFailedResponse(bootee, shouldBan, BOOT_NOT_SUPPORTED, 
                                                gameChannel);
    	    return;
    	}
    	if (shouldBan && !lobby.getCanHostBan()) {
    	    sendBootFailedResponse(bootee, shouldBan, BAN_NOT_SUPPORTED, 
                                                                gameChannel);
    	    return;
    	}
    	if (clientSession.getName().equals(bootee)) {
    	    sendBootFailedResponse(bootee, shouldBan, BOOT_SELF, gameChannel);
    	    return;
    	}
    	if (!gameRoom.getHost().equals(clientSession)) {
    	    sendBootFailedResponse(bootee, shouldBan, PLAYER_NOT_HOST, 
                                                                gameChannel);
    	    return;
    	}
    	
    	CommandList list = new CommandList(PLAYER_BOOTED_FROM_GAME);
    	list.add(clientSession.getName());
    	list.add(bootee);
    	list.add(shouldBan);
    	
    	// send the message notification before actually booting so that the boot
    	// message arrives before the 'left channel' message.
    	sendMulticastResponse(gameRoom.getUsers(), list, gameChannel);
    	
        Channel lobbyChannel = AppContext.getChannelManager().getChannel(
                                                        lobby.getChannelName());
        sendMulticastResponse(lobby.getUsers(), list, lobbyChannel);

        ClientSession session = gameRoom.getSession(bootee);
    	gameRoom.bootPlayer(bootee, shouldBan);
    	doLeaveGame(gameRoom, session);
    }
    
    private void sendGameUpdateFailedResponse(String gameName, 
    		String gameDescription, HashMap<String, ByteWrapper> params, 
    		int errorCode, String channelName) {
    	
    	CommandList list = new CommandList(UPDATE_GAME_FAILED);
    	list.add(gameName);
    	list.add(gameDescription);
    	packGameParameters(list, params);
    	list.add(new UnsignedByte(errorCode));
        
        Channel channel = 
                    AppContext.getChannelManager().getChannel(channelName);
    	
    	sendResponse(list, channel);
    }
    
    private void sendBootFailedResponse(String bootee, boolean isBanned, 
                                            int errorCode, Channel channel) {
    	CommandList list = new CommandList(BOOT_FAILED);
    	list.add(bootee);
    	list.add(isBanned);
    	list.add(new UnsignedByte(errorCode));
    	
    	sendResponse(list, channel);
    }

    /**
     * Attempts to join this user to the Lobby channel specified in the
     * data buffer. If the lobby is password protected, then a password
     * is read off the buffer and compared.
     * <p>
     * The contents of the message are:
     * <ul>
     * <li>The lobby name as a String</li>
     * </ul>
     * 
     * @param data the buffer containing the command parameters
     */
    private void joinLobby(ByteBuffer data) {
        if (currentLobby != null) {
            sendErrorResponse(CONNECTED_LOBBY);
            return;
        }

        String lobbyName = protocol.readString(data);
        ManagedMap<String, ManagedReference> lobbyMap =
            AppContext.getDataManager().getBinding("LobbyMap", 
                                                        ManagedMap.class);
        ManagedReference lobbyRef = lobbyMap.get(lobbyName);
        if (lobbyRef == null) { 
            sendErrorResponse(INVALID_LOBBY);
            return;
        }
        Lobby lobby = lobbyRef.get(Lobby.class);
        if (lobby.getMaxPlayers() > 0 && lobby.getNumPlayers() >= 
                                                        lobby.getMaxPlayers()) {
            sendErrorResponse(MAX_PLAYERS);
            return;
        }
        if (lobby.isPasswordProtected()) {
            String password = data.hasRemaining() ? 
                                        protocol.readString(data) : null;
            if (password == null || !lobby.getPassword().equals(password)) {
                sendErrorResponse(INCORRECT_PASSWORD);
                return;
            }
        } 
        Channel channel = 
            AppContext.getChannelManager().getChannel(lobby.getChannelName());
        channel.join(clientSession, null);
        currentLobby = lobbyRef;
        lobby.addUser(clientSession);
        
        
        // send the games to the user
        for (ManagedReference gRef : lobby.getGameRoomList()) {
            GameRoom curGame = gRef.get(GameRoom.class);
            CommandList list = new CommandList(GAME_CREATED);
            packGameDescriptor(list, curGame);
            sendResponse(list, channel);
        }
        
        // send lobby joined message
        CommandList list = new CommandList(PLAYER_ENTERED_LOBBY);
        list.add(clientSession.getSessionId());
        list.add(clientSession.getName());
        sendMulticastResponse(lobby.getUsers(), list, channel);
        
        
        sendRetroJoins(lobby.getUsers(), PLAYER_ENTERED_LOBBY, channel);
        
        // send retro joins for users joining games in the lobby
        // this must be sent last in order to receive the user names
        // from the PLAYER_ENTERED_LOBBY retrojoins.
        for (ManagedReference grRef : lobby.getGameRoomList()) {
            GameRoom gr = grRef.get(GameRoom.class);
            for (ClientSession curUser : gr.getUsers()) {
                CommandList joinedGameList = 
                                            new CommandList(PLAYER_JOINED_GAME);
                joinedGameList.add(clientSession.getSessionId());
                joinedGameList.add(clientSession.getName());
                joinedGameList.add(gr.getName());
                sendResponse(joinedGameList, channel);
            }
        } 
    }
 
    /**
     * Sends a response of ERROR on the control channel with a message
     * detailing the failure.
     * 
     * @param errorCode			the reason for the error
     */
    private void sendErrorResponse(int errorCode) {
        CommandList list = new CommandList(ERROR);
        list.add(new UnsignedByte(errorCode));
        sendResponse(list);
    }

    private boolean checkGameRoom() {
        if (currentGameRoom == null) {
            sendErrorResponse(NOT_CONNECTED_GAME);
            return false;
        }
        return true;
    }
    
    private boolean checkLobby() {
        if (currentLobby == null) {
            sendErrorResponse(NOT_CONNECTED_LOBBY);
            return false;
        }
        return true;
    }
    
    /**
     *  Attempts to start the game that this player is connected to.  A series
     *  of checks is initiated, for example, to check that the player is the
     *  host of the game, and that all players have indicated that they are
     *  ready.  If the game cannot be started for any of these reasons, an
     *  appropriate error message is sent back to the player.
     *
     */
    private void startGame() {
    	if (!checkGameRoom()) {
    	    return;
    	}
    	if (!checkLobby()) {
    	    return;
    	}

        GameRoom gameRoom = currentGameRoom.getForUpdate(GameRoom.class);
        ClientSession host = gameRoom.getHost();
        // only the host can start the game.
        if (!host.equals(clientSession)) {
            sendErrorResponse(PLAYER_NOT_HOST);
            return;
        }
        if (!gameRoom.arePlayersReady()) {
            sendErrorResponse(PLAYERS_NOT_READY);
            return;
        }
        Lobby lobby = gameRoom.getLobby().getForUpdate(Lobby.class);
        if (lobby.getMinPlayersInGameRoomStart() > 0 && 
            gameRoom.getNumPlayers() < lobby.getMinPlayersInGameRoomStart()) {
            
            sendErrorResponse(LESS_THAN_MIN_PLAYERS);
            return;
        }

        if (lobby.getMaxPlayersInGameRoomStart() > 0 &&
            gameRoom.getNumPlayers() > lobby.getMaxPlayersInGameRoomStart()) {
            
            sendErrorResponse(GREATER_THAN_MAX_PLAYERS);
            return;
        }
        
        lobby.removeGameRoom(currentGameRoom);
        
        CommandList list = new CommandList(GAME_STARTED);
        packGameDescriptor(list, gameRoom);

        // send identical message to both game room and lobby
        
        sendMulticastResponse(gameRoom.getUsers(), list, 
                AppContext.getChannelManager().getChannel(
                                    gameRoom.getChannelName()));
        sendMulticastResponse(lobby.getUsers(), list, 
                AppContext.getChannelManager().getChannel(
                                    lobby.getChannelName()));

        gameRoom.setStarted(true);
        
    }
    
    /**
     * Sent to the player in response to a start game command that was not
     * able to be completed.
     * 
     * @param reason            the reason for failure
     * @param channel           the game channel to send the message on
     */
    private void sendStartGameFailed(String reason, Channel channel) {
        CommandList list = new CommandList(START_GAME_REQUEST);
        list.add(reason);

        sendResponse(list, channel);
    }
    
    /**
     * Reads game parameters from the given buffer into a map.  The expected
     * format of the message is:
     * 
     * <ul>
     * <li>number of parameters as an int</li>
     * <li>each parameter according to its type</li>
     * </ul>
     * 
     * @param data
     * @return
     */
    private HashMap<String, ByteWrapper> readGameParameters(ByteBuffer data) {
        int numParams = data.getInt();
        HashMap<String, ByteWrapper> gameParams = new HashMap<String, ByteWrapper>();
        for (int i = 0; i < numParams; i++) {
            String key = protocol.readString(data);
            ByteWrapper value = protocol.readParamValue(data);
            gameParams.put(key, value);
        }
        return gameParams;
    }

    /**
     * Updates the ready-state of this player.
     * 
     * The contents of the message are:
     * <ul>
     * <li>the player's updated ready-state as a boolean</li>
     * <li>the game parameters of the game that the player is agreeing 
     * to play</li>
     * </ul> 
     * 
     * @param data
     */
    private void updatePlayerReadyRequest(ByteBuffer data) {
    	if (!checkGameRoom()) {
    	    return;
    	}
        boolean ready = protocol.readBoolean(data);
        GameRoom gameRoom = currentGameRoom.getForUpdate(GameRoom.class);
        if (ready) {
            HashMap<String, ByteWrapper> gameParams = readGameParameters(data);

            Map<String, ByteWrapper> masterParameters = 
                                                    gameRoom.getGameParamters();

            // bail out if all of the parameters are not equal.
            if (!gameParams.equals(masterParameters)) {
                sendPlayerReadyUpdate(false, gameRoom);
                return;
            }

            // all criteria has been met, the play is indeed ready (or not).
            gameRoom.updateReady(clientSession, ready);
        }
        sendPlayerReadyUpdate(ready, gameRoom);
    }

    private void sendPlayerReadyUpdate(boolean ready, GameRoom game) {
        CommandList list = new CommandList(PLAYER_READY_UPDATE);
        list.add(clientSession.getName());
        list.add(ready);

        sendMulticastResponse(game.getUsers(), list, 
             AppContext.getChannelManager().getChannel(game.getChannelName()));
    }

    /**
     * Attempts to join this user to the Game Room channel specified in
     * the data buffer. If the game is password protected, then a
     * password is read off the buffer and compared.  The game room's list
     * of banned players is also checked against the attempted joinee.
     * 
     * @param data the buffer containing the command parameters
     */
    private void joinGame(ByteBuffer data) {
        if (currentGameRoom != null) { // can't connect if already
                                        // connected to a game.
            sendErrorResponse(CONNECTED_GAME);
            return;
        }
        String gameName = protocol.readString(data);

        ManagedMap<String, ManagedReference> gameRoomMap =
            AppContext.getDataManager().getBinding("GameRoomMap", 
                                                    ManagedMap.class);
        
        ManagedReference gameRef = gameRoomMap.get(gameName);
        if (gameRef == null) {
            sendErrorResponse(INVALID_GAME);
            return;
        }
        GameRoom gameRoom = gameRef.get(GameRoom.class);
        if (gameRoom.isPasswordProtected()) {
            String password = data.hasRemaining() ? 
                                        protocol.readString(data) : null;
                                        
            if (password == null || !password.equals(gameRoom.getPassword())) {
            	sendErrorResponse(INCORRECT_PASSWORD);
                return;
            }
        }
        if (gameRoom.getMaxPlayers() > 0 && 
                    gameRoom.getNumPlayers() >= gameRoom.getMaxPlayers()) {
            
            sendErrorResponse(MAX_PLAYERS);
            return;
        }
        if (gameRoom.isPlayerBanned(clientSession)) {
            sendErrorResponse(PLAYER_BANNED);
            return;
        }
        Channel channel = AppContext.getChannelManager().getChannel(
                                                gameRoom.getChannelName());
        channel.join(clientSession, null);
        doJoinGame(gameRoom.getName());
    }

    /**
     * Called when a user requests the parameters for a game on the
     * currently connected lobby. The GameParametersResponse command is
     * sent as the response.
     */
    private void gameParametersRequest() {
    	if (!checkLobby()) {
    	   return;
    	}
        CommandList list = new CommandList(GAME_PARAMETERS_RESPONSE);
        Lobby lobby = currentLobby.get(Lobby.class);
        list.add(lobby.getChannelName());
        Map<String, ByteWrapper> gameParameters = lobby.getGameParamters();
        list.add(gameParameters.size());
        for (String curKey : gameParameters.keySet()) {
            list.add(curKey);
            ByteWrapper value = gameParameters.get(curKey);
            //list.add(protocol.mapType(value));
            list.add(value.getType());
            list.add(value);

        }

        sendResponse(list);
    }

    /**
     * <p>
     * Processes a request to create a new game room in the user's
     * current lobby. If the game can not be created for any reason, a
     * CreateGameFailed response is sent back to the user with a reason
     * for the failure.
     * </p>
     * 
     * <p>
     * If creation is successful, the new GameRoom object is added to
     * the lobby and connected users are notified via the GameCreated
     * response.
     * </p>
     * 
     * The contents of the message are:
     * <ul>
     * <li>The game name as a String</li>
     * <li>The game description as a String</li>
     * <li>whether or not this game is password protected as a boolean</li>
     * <li>the password, if password protected as a String</li>
     * <li>the game parameters</li>
     * </ul>
     * 
     * @param data the buffer containing the request data
     */
    private void createGame(ByteBuffer data) {
        String gameName = protocol.readString(data);
        if (currentLobby == null) { // bail out early if not connected.
            sendGameCreateFailedResponse(null, gameName, NOT_CONNECTED_LOBBY);
            return;
        }
        Lobby lobby = currentLobby.getForUpdate(Lobby.class);
        if (currentGameRoom != null) {
            sendGameCreateFailedResponse(lobby.getChannelName(), gameName, 
                                        CONNECTED_GAME);
            return;
        }
        if (!filterGameName(gameName)) {
            sendGameCreateFailedResponse(lobby.getChannelName(), gameName, 
						INVALID_GAME_NAME);
            return;
        }
        
        for (ManagedReference grRef : lobby.getGameRoomList()) {
            GameRoom gr = grRef.get(GameRoom.class);
            if (gr.getName().equals(gameName)) {
            	sendGameCreateFailedResponse(lobby.getChannelName(), 
                                                    gameName, GAME_EXISTS);
            	return;
            }
        }

        String description = protocol.readString(data);
        boolean hasPassword = protocol.readBoolean(data);
        String password = null;
        if (hasPassword) {
            password = protocol.readString(data);
        }

        HashMap<String, ByteWrapper> gameParams = readGameParameters(data);

        Map<String, ByteWrapper> lobbyParameters = lobby.getGameParamters();

        // bail out if all of the expected parameters are not present.
        if (!gameParams.keySet().equals(lobbyParameters.keySet())) {
            sendGameCreateFailedResponse(lobby.getChannelName(), gameName, 
                    				INVALID_GAME_PARAMETERS);
            return;
        }

        String channelName = lobby.getChannelName() + ":" + gameName;
        Channel channel = AppContext.getChannelManager().createChannel(
                                        channelName, null, Delivery.RELIABLE);

        ManagedReference grRef = AppContext.getDataManager().createReference(
                           new GameRoom(gameName, description, password, 
                            channelName, clientSession, currentLobby));
        lobby.addGameRoom(grRef);

        GameRoom gr = grRef.get(GameRoom.class);
        gr.setMaxPlayers(lobby.getMaxPlayersInGameRoom());

        
        // add to the game room map for easy look-up by game name
        ManagedMap<String, ManagedReference> gameRoomMap =
            AppContext.getDataManager().getBinding("GameRoomMap", 
                                                        ManagedMap.class);
        gameRoomMap.put(gr.getName(), grRef);

        for (Map.Entry<String, ByteWrapper> entry : gameParams.entrySet()) {
            gr.addGameParameter(entry.getKey(), entry.getValue());
        }

        CommandList list = new CommandList(GAME_CREATED);
        packGameDescriptor(list, gr);

        Channel lobbyChannel = 
            AppContext.getChannelManager().getChannel(lobby.getChannelName());
        sendMulticastResponse(lobby.getUsers(), list, lobbyChannel);

        // join the user that created this game as the host after the
        // CREATE_GAME message has been sent.
        channel.join(clientSession, null);
        doJoinGame(gameName);
    }
    
    public void doJoinGame(String gameName) {
        Lobby lobby = currentLobby.get(Lobby.class);
        ManagedReference gameRef = null;
        for (ManagedReference curGameRef : lobby.getGameRoomList()) {
            GameRoom gr = curGameRef.get(GameRoom.class);
            if (gr.getName().equals(gameName)) {
                gameRef = curGameRef;
                break;
            }
        }
        if (gameRef != null) {
            currentGameRoom = gameRef;
            GameRoom gameRoom = gameRef.getForUpdate(GameRoom.class);
            gameRoom.addUser(clientSession);
            
            // send playerJoinedGame message to lobby
            CommandList lobbyList = new CommandList(PLAYER_JOINED_GAME);
            lobbyList.add(clientSession.getSessionId());
            lobbyList.add(clientSession.getName());
            lobbyList.add(gameRoom.getName());

            Channel lobbyChannel = 
              AppContext.getChannelManager().getChannel(lobby.getChannelName());
            sendMulticastResponse(lobby.getUsers(), lobbyList, lobbyChannel);

            // send playerEnteredGame to game room
            CommandList grList = new CommandList(PLAYER_ENTERED_GAME);
            grList.add(clientSession.getSessionId());
            grList.add(clientSession.getName());
            
            Channel gameChannel = AppContext.getChannelManager().getChannel(
                                                    gameRoom.getChannelName());
            sendRetroJoins(gameRoom.getUsers(), PLAYER_ENTERED_GAME, 
                                                                gameChannel);
            
            sendMulticastResponse(gameRoom.getUsers(), grList, gameChannel);
            
            // send the 'ready' state (if true) of each player already connected
            for (ClientSession curUser : gameRoom.getUsers()) {
                
                boolean isReady = gameRoom.isPlayerReady(curUser);
                if (curUser.equals(clientSession) || !isReady) {
                    continue;
                }
                CommandList list = new CommandList(PLAYER_READY_UPDATE);
                list.add(curUser.getName());
                list.add(isReady);
                
                sendResponse(list, gameChannel);
            }                    
        }
    }
     
    /**
     * Filters the given name against simple criteria. 
     * 
     * @param name
     * 
     * @return true if the given name passes the filter
     */
    private boolean filterGameName(String name) {
    	if (name == null || name.length() == 0) {
    	    return false;
    	}
    	for (int i = 0; i < name.length(); i++) {
            if (!Character.isWhitespace(name.charAt(i))) {
            	return true;
            }
    	}
    	return false;
    }

    public void disconnected(boolean graceful) {
        // Remove the associated Player binding from the datastore. 
        AppContext.getDataManager().removeBinding(clientSession.getName());
    }

    /**
     * Puts the details of a game room into the given list for
     * transport.
     * 
     * The contents of the message are:
     * <ul>
     * <li>the game name as a String</li>
     * <li>the game description as a String</li>
     * <li>the channel name of the game as a String</li>
     * <li>whether or not the game is password protected as a boolean</li>
     * <li>the maximum number of players as an int</li>
     * <li>the game parameters</li>
     * </ul>
     * 
     * @param list the list in which to put the game details
     * @param game the game
     */
    private void packGameDescriptor(CommandList list, GameRoom game) {
        list.add(game.getName());
        list.add(game.getDescription());
        list.add(game.getChannelName());
        list.add(game.isPasswordProtected());
        list.add(game.getMaxPlayers());

        packGameParameters(list, game.getGameParamters());
    }
    
    /**
     * Puts the parameters of a game into the given CommandList.
     * 
     * The contents of the parameters are:
     * <ul>
     * <li>the number of parameters as an int</li>
     * <li>for each parameter:</li>
     * <li>the parameter name</li>
     * <li>the type of parameter(String, etc.)</li>
     * <li>the value of the parameter</li>
     * </ul>
     * 
     * @param list
     * @param gameParams
     */
    private void packGameParameters(CommandList list, 
                                        Map<String, ByteWrapper> gameParams) {
        list.add(gameParams.size());

        for (Map.Entry<String, ByteWrapper> entry : gameParams.entrySet()) {
            String curKey = entry.getKey();
            list.add(curKey);
            ByteWrapper value = entry.getValue();
            list.add(value.getType());
            list.add(value);
        }
    }

    private void sendGameCreateFailedResponse(String lobbyChannelName, 
                                            String gameName, int errorCode) {
    	
        CommandList list = new CommandList(CREATE_GAME_FAILED);
        list.add(gameName);
        list.add(new UnsignedByte(errorCode));
        list.add(lobbyChannelName);

        sendResponse(list);
    }
    
    
    private void sendResponse(CommandList list) {
        clientSession.send(list.getByteArray());
    }
    
    private void sendMulticastResponse(List<ClientSession> users, 
                                        CommandList list, Channel channel) {

        Set<ClientSession> set = new HashSet<ClientSession>();
        for (ClientSession curSession : users) {
            set.add(curSession);
        }
        channel.send(set, list.getByteArray());
    }

    private void sendResponse(CommandList list, Channel channel) {
        channel.send(clientSession, list.getByteArray());
    }
    
}
