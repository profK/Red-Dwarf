package com.sun.sgs.example.swarm.server.factories;

import java.util.Map;
import java.util.Iterator;

import com.sun.sgs.app.ManagedReference;

import com.sun.sgs.example.swarm.server.domain.Building;
import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.domain.AttackPlayer;
import com.sun.sgs.example.swarm.server.domain.BuildPlayer;
import com.sun.sgs.example.swarm.server.domain.BoardItem;
import com.sun.sgs.example.swarm.server.domain.Board;
import com.sun.sgs.example.swarm.server.domain.GameRoom;
import com.sun.sgs.example.swarm.server.domain.LobbyRoom;

import com.sun.sgs.example.swarm.shared.data.BuildingDTO;
import com.sun.sgs.example.swarm.shared.data.PlayerDTO;
import com.sun.sgs.example.swarm.shared.data.AttackPlayerDTO;
import com.sun.sgs.example.swarm.shared.data.BuildPlayerDTO;
import com.sun.sgs.example.swarm.shared.data.BoardItemDTO;
import com.sun.sgs.example.swarm.shared.data.BoardDTO;
import com.sun.sgs.example.swarm.shared.data.GameRoomDTO;
import com.sun.sgs.example.swarm.shared.data.LobbyRoomDTO;
import com.sun.sgs.example.swarm.shared.data.GameRoomStatsDTO;

/**
 * Converts domain data to DTO data for shipping original snapshots of
 * items to the client
 */
public class DataTransformer 
{
    
    public static BuildingDTO generateBuildingDTO(Building building)
    {
        return new BuildingDTO(building.getStrength(), building.getTeam());
    }
    
    public static PlayerDTO generatePlayerDTO(Player player)
    {
        if(player instanceof AttackPlayer)
            return new AttackPlayerDTO(player.getUid(), player.getStrength(), player.getTeam());
        else if(player instanceof BuildPlayer)
            return new BuildPlayerDTO(player.getUid(), player.getStrength(), player.getTeam());
        
        return null;
    }
    
    public static BoardItemDTO generateBoardItemDTO(BoardItem item)
    {
        if(item instanceof Building)
            return generateBuildingDTO((Building)item);
        else if(item instanceof Player)
            return generatePlayerDTO((Player)item);
        
        return null;
    }
    
    public static BoardDTO generateBoardDTO(Board board)
    {
        BoardDTO newBoard = new BoardDTO(board.getRows(), board.getCols());
        for(int r = 0; r < board.getRows(); r++) {
            for(int c = 0; c < board.getRows(); c++) {
                Map<String, ManagedReference<BoardItem>> oldItems = board.getBoardItems(r, c).get();
                Map<String, BoardItemDTO> newItems = newBoard.getBoardItems(r, c);
                for(Iterator<String> ik = oldItems.keySet().iterator(); ik.hasNext(); ) {
                    String key = ik.next();
                    BoardItem item = oldItems.get(key).get();
                    newItems.put(key, generateBoardItemDTO(item));
                }
            }
        }
        
        return newBoard;
    }
    
    public static GameRoomDTO generateGameRoomDTO(GameRoom gameRoom)
    {
        BoardDTO board = generateBoardDTO(gameRoom.getBoardRef().get());
        return new GameRoomDTO(gameRoom.getName(), gameRoom.getMaxPlayers(), gameRoom.getCurrentPlayers(), board, gameRoom.getPlayerLocations());
    }
    
    public static LobbyRoomDTO generateLobbyRoomDTO(LobbyRoom lobbyRoom)
    {
        LobbyRoomDTO lobby = new LobbyRoomDTO(lobbyRoom.getMaxPlayers(), lobbyRoom.getCurrentPlayers());
        
        for(int i = 0; i < lobbyRoom.getTotalRooms(); i++) {
            GameRoom game = lobbyRoom.getRoom(i).get();
            lobby.getGameRooms().add(new GameRoomStatsDTO(game.getName(), game.getMaxPlayers(), game.getCurrentPlayers()));
        }
        return lobby;
    }
    
}
