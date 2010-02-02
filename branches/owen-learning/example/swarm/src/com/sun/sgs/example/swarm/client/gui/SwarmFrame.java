package com.sun.sgs.example.swarm.client.gui;

import java.util.Observer;
import java.util.Observable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

import com.sun.sgs.example.swarm.client.model.SwarmModel;
import com.sun.sgs.example.swarm.client.model.LobbyRoomModel;

/**
 *
 * @author ok194946
 */
public class SwarmFrame extends JFrame implements Observer
{
    private SwarmModel swarmModel;
    
    private JPanel mainPanel;
    private CardLayout layout;
    
    private LobbyPanel lobbyPanel;
    
    /** Creates a new instance of SwarmFrame */
    public SwarmFrame(SwarmModel swarmModel)
    {
        this.swarmModel = swarmModel;
        swarmModel.addObserver(this);
        
        build();
    }
    
    private void build()
    {
        mainPanel = new JPanel();
        layout = new CardLayout();
        lobbyPanel = new LobbyPanel(swarmModel.getLobbyRoomModel());
        mainPanel.setLayout(layout);
        mainPanel.add("Lobby", lobbyPanel);
        
        getContentPane().add(mainPanel);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500,500);
        this.setLocationRelativeTo(null);

        this.setVisible(true);
    }
    
    public void update(Observable source, Object arg)
    {
        layout.show(mainPanel, "Lobby");
    }
}
