package com.sun.sgs.example.swarm.client.gui;

import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import com.sun.sgs.example.swarm.client.model.LobbyRoomModel;

/**
 *
 * @author ok194946
 */
public class LobbyPanel extends JPanel implements Observer
{
    private LobbyRoomModel lobbyRoomModel;
    
    private JLabel lobbyNameLabel = new JLabel("Welcome to the Swarm Game Lobby!");
    private JLabel maxPlayersLabel = new JLabel("Maximum Players Allowed : ");
    private JLabel currentPlayersLabel = new JLabel("Current Players Connected : ");
    private JLabel gameRoomListLabel = new JLabel("Swarm Games in Progress : ");
    
    private JLabel maxPlayers = new JLabel();
    private JLabel currentPlayers = new JLabel();
    private JList gameRoomList;
    
    /** Creates a new instance of LobbyPanel */
    public LobbyPanel(LobbyRoomModel lobbyRoomModel)
    {
        this.lobbyRoomModel = lobbyRoomModel;
        lobbyRoomModel.addObserver(this);
        this.gameRoomList = new JList(new GameRoomStatsListModel(lobbyRoomModel));
        
        this.setLayout(new BorderLayout(5,5));
        this.add(createLobbyStatsPanel(), BorderLayout.NORTH);
        this.add(createGameRoomListPanel(), BorderLayout.CENTER);
    }
    
    private JPanel createLobbyStatsPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,1,5,5));
        
        Dimension d1 = maxPlayersLabel.getMinimumSize();
        Dimension d2 = currentPlayersLabel.getMinimumSize();
        Dimension size = new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
        maxPlayersLabel.setPreferredSize(size);
        currentPlayersLabel.setPreferredSize(size);
        
        JPanel maxPlayersPanel = new JPanel();
        maxPlayersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        maxPlayersPanel.add(maxPlayersLabel);
        maxPlayersPanel.add(maxPlayers);
        
        JPanel currentPlayersPanel = new JPanel();
        currentPlayersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        currentPlayersPanel.add(currentPlayersLabel);
        currentPlayersPanel.add(currentPlayers);
        
        panel.add(lobbyNameLabel);
        panel.add(maxPlayersPanel);
        panel.add(currentPlayersPanel);
        
        return panel;
    }
    
    private JPanel createGameRoomListPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5,5));
        
        panel.add(gameRoomListLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(gameRoomList), BorderLayout.CENTER);
        
        return panel;
    }
    
    public void update(Observable source, Object arg)
    {
        //update the lobby player stats
        maxPlayers.setText(String.valueOf(lobbyRoomModel.getLobbyRoom().getMaxPlayers()));
        currentPlayers.setText(String.valueOf(lobbyRoomModel.getLobbyRoom().getCurrentPlayers()));
    }
    
}
