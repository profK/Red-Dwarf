package com.sun.sgs.example.swarm.client;

import java.util.Map;
import java.util.HashMap;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;

import com.sun.sgs.client.simple.SimpleClientListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;

import com.sun.sgs.example.swarm.client.model.SwarmModel;
import com.sun.sgs.example.swarm.client.factories.MessageSender;
import com.sun.sgs.example.swarm.shared.messages.JoinLobbyMessage;
import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;
import com.sun.sgs.example.swarm.shared.data.RoomDTO;
import com.sun.sgs.example.swarm.shared.data.LobbyRoomDTO;
import com.sun.sgs.example.swarm.shared.data.GameRoomDTO;
import com.sun.sgs.example.swarm.shared.results.Result;
import com.sun.sgs.example.swarm.shared.results.SnapshotResult;
import com.sun.sgs.example.swarm.shared.results.BuildingUpdatedResult;
import com.sun.sgs.example.swarm.shared.results.PlayerMovedResult;
import com.sun.sgs.example.swarm.shared.results.PlayerUpdatedResult;
import com.sun.sgs.example.swarm.shared.util.ByteBufferInputStream;


public class SwarmClient implements SimpleClientListener, ClientChannelListener
{
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(SwarmClient.class.getName());
    
    private SimpleClient simpleClient;
    private SwarmModel swarmModel;
    private Map<String, ClientChannel> clientChannelMap;
    
    private String username;
    private String password;
    private Team team;
    private PlayerType playerType;
    
    public SwarmClient()
    {
        this.simpleClient = new SimpleClient(this);
        this.swarmModel = new SwarmModel();
        this.clientChannelMap = new HashMap<String, ClientChannel>();
    }
    
    public void login(String username, String password, Team team, PlayerType playerType)
    {
        this.username = username;
        this.password = password;
        this.team = team;
        this.playerType = playerType;
        
        
        //login
        String host = System.getProperty("host");
        String port = System.getProperty("port");
        try {
            Properties props = new Properties();
            props.setProperty("host", host);
            props.setProperty("port", port);
            simpleClient.login(props);
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unable to connect", e);
        }
    }
    
    public SwarmModel getSwarmModel() { return swarmModel; }
    public String getUsername() { return username; }
    public Team getTeam() { return team; }
    public PlayerType getPlayerType() { return playerType; }
    
    public PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(username, password.toCharArray());
    }
    
    public void loggedIn()
    {
        //login was successful so try and join the lobby
        logger.log(Level.INFO, "Login successful");
        MessageSender.sendMessage(new JoinLobbyMessage(team, playerType), simpleClient);
    }
    
    public void loginFailed(String reason)
    {
        
    }
    
    public void disconnected(boolean graceful, String reason)
    {
        
    }
    
    public ClientChannelListener joinedChannel(ClientChannel channel)
    {
        clientChannelMap.put(channel.getName(), channel);
        return this;
    }
    
    public void receivedMessage(ByteBuffer buffer)
    {
        processMessage(buffer);
    }
    
    public void reconnected()
    {
        
    }
    
    public void reconnecting()
    {
        
    }
    
    public void leftChannel(ClientChannel channel)
    {
        clientChannelMap.remove(channel);
    }
    
    public void receivedMessage(ClientChannel channel, ByteBuffer buffer)
    {
        processMessage(buffer);
    }
    
    
    private void processMessage(ByteBuffer buffer)
    {
        try {
            //deserialize the message
            ObjectInputStream input = new ObjectInputStream(new ByteBufferInputStream(buffer));
            Result result = (Result)input.readObject();
            
            //process the message
            handleResult(result);
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unable to process message", e);
        }
    }
    
    private void handleResult(Result result)
    {
        logger.log(Level.INFO, "Result message received");
        
        if(result instanceof SnapshotResult) {
            RoomDTO room = ((SnapshotResult)result).getRoom();
            if(room instanceof LobbyRoomDTO) {
                swarmModel.notifyRoomSwitched();
                swarmModel.getLobbyRoomModel().setLobbyRoom((LobbyRoomDTO)room);
            }
        }
    }

    
}