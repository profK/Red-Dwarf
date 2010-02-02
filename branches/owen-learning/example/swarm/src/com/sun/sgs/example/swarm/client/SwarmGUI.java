package com.sun.sgs.example.swarm.client;

import com.sun.sgs.example.swarm.client.gui.SwarmFrame;
import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;

/**
 *
 * @author ok194946
 */
public class SwarmGUI
{
    
    public static void main(String[] args)
    {
        SwarmClient client = new SwarmClient();
        SwarmFrame frame = new SwarmFrame(client.getSwarmModel());
        client.login("owen", "test", Team.BLUE, PlayerType.ATTACKER);
    }
    
}
