package model.game;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import model.item.Inventory;
import model.board.Board;
import model.game.TurnController;
import model.config.CryptoUtil;
import model.entity.Entity;
import model.entity.Player;
import model.entity.Seal;
import model.db.BBDD;

public class SaveLoadService {

    public static boolean saveGame(String gameId, Board board, TurnController turnController, Seal seal) {
        Map<String, Object> state = new HashMap<>();
        
        // 1. Board state
        List<String> boardStr = new ArrayList<>();
        for (int i = 0; i < Board.MAX_SQUARES; i++) {
        	boardStr.add(board.getSquareType(i).name());
        }
        state.put("board", boardStr);
        
        // 2. Turn state
        state.put("currentTurn", turnController.getCurrentTurnIndex());
        
        // 3. Players
        List<Map<String, Object>> playersList = new ArrayList<>();
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player) {
                Player p = (Player) e;
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("id", p.getEntityId());
                pMap.put("name", p.getName());
                pMap.put("color", p.getColour());
                pMap.put("password", p.getPassword());
                pMap.put("square", p.getSquareIndex());
                pMap.put("skipNextTurn", p.shouldSkipNextTurn());
                if (p.getAvatarPath() != null) {
                    pMap.put("avatarPath", p.getAvatarPath());
                }
                
                Inventory inv = p.getInventory();
                Map<String, Integer> invMap = new HashMap<>();
                invMap.put("snowballs", inv.getSnowballQuantity());
                invMap.put("fish", inv.getFishQuantity());
                invMap.put("fastdice", inv.getFastdiceQuantity());
                invMap.put("slowdice", inv.getSlowdiceQuantity());
                pMap.put("inventory", invMap);
                
                if (p.getLastEvent() != null) {
                    pMap.put("lastEventType", p.getLastEvent().getType().name());
                    pMap.put("lastEventDetail", p.getLastEvent().getDetail());
                    pMap.put("lastEventPos", p.getLastEvent().getNewPosition());
                }
                
                playersList.add(pMap);
            }
        }
        state.put("players", playersList);
        
        // 4. Seal
        if (seal != null) {
            Map<String, Object> sealMap = new HashMap<>();
            sealMap.put("square", seal.getSquareIndex());
            sealMap.put("blockedTurns", seal.getBlockedTurns());
            state.put("seal", sealMap);
        }
        
        try {
            // Serialize
            Yaml yaml = new Yaml();
            String yamlString = yaml.dump(state);
            
            // Encrypt
            String encrypted = CryptoUtil.encrypt(yamlString);
            if (encrypted == null) {
                System.out.println("Encryption failed.");
                return false;
            }
            
            // DB Save
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
            	// Try to create table if it doesn't exist (ignore error if exists)
            	String createSql = "CREATE TABLE SAVED_GAMES (GAME_ID VARCHAR2(50) PRIMARY KEY, GAME_DATA CLOB)";
            	try {
            		java.sql.Statement st = con.createStatement();
            		st.execute(createSql);
            		st.close();
            	} catch (Exception e) {} // already exists
            	
            	// 1. Generamos un ID único añadiendo los milisegundos a la palabra PARTIDA
            	String idUnico = "PARTIDA_" + System.currentTimeMillis();

            	// 2. Ya no hacemos DELETE. Directamente hacemos el INSERT con el nuevo idUnico
            	BBDD.insert(con, "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) VALUES ('" + idUnico + "', '" + encrypted + "')");
            	BBDD.cerrar(con);
            	
            	System.out.println("Game Saved Successfully!");
                return true;
            } else {
            	System.out.println("Could not connect to DB.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean loadGame(String gameId) {
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con == null) {
                System.out.println("Could not connect to DB.");
                return false;
            }

            java.util.ArrayList<java.util.LinkedHashMap<String, String>> result = BBDD.select(con, "SELECT GAME_DATA FROM SAVED_GAMES WHERE GAME_ID = '" + gameId + "'");
            BBDD.cerrar(con);

            if (result.isEmpty()) {
                System.out.println("No saved game found with ID: " + gameId);
                return false;
            }

            String encrypted = result.get(0).get("GAME_DATA");
            if (encrypted == null || encrypted.isEmpty()) {
                System.out.println("Saved game data is empty.");
                return false;
            }

            String yamlString = CryptoUtil.decrypt(encrypted);
            if (yamlString == null) {
                System.out.println("Decryption failed.");
                return false;
            }

            Yaml yaml = new Yaml();
            Map<String, Object> state = yaml.load(yamlString);

            // Populate GameSetupConfig
            model.config.GameSetupConfig.setLoadedGame(true);
            model.config.GameSetupConfig.setLoadedBoardState((List<String>) state.get("board"));
            model.config.GameSetupConfig.setLoadedTurnIndex(((Number) state.get("currentTurn")).intValue());

            List<Map<String, Object>> playersList = (List<Map<String, Object>>) state.get("players");
            List<Player> players = new ArrayList<>();
            for (Map<String, Object> pMap : playersList) {
                Player p = new Player((String) pMap.get("name"), (String) pMap.get("color"), (String) pMap.get("password"));
                p.setEntityId(((Number) pMap.get("id")).intValue());
                p.setSquare(((Number) pMap.get("square")).intValue());
                p.setSkipNextTurn((Boolean) pMap.get("skipNextTurn"));
                if (pMap.containsKey("avatarPath")) {
                    p.setAvatarPath((String) pMap.get("avatarPath"));
                }

                Map<String, Integer> invMap = (Map<String, Integer>) pMap.get("inventory");
                Inventory inv = p.getInventory();
                inv.setSnowballQuantity(invMap.get("snowballs"));
                inv.setFishQuantity(invMap.get("fish"));
                inv.setFastdiceQuantity(invMap.get("fastdice"));
                inv.setSlowdiceQuantity(invMap.get("slowdice"));
                inv.setDiceQuantity(invMap.get("fastdice") + invMap.get("slowdice"));
                
                if (pMap.containsKey("lastEventType")) {
                    model.board.EventManager.EventType type = model.board.EventManager.EventType.valueOf((String) pMap.get("lastEventType"));
                    String detail = (String) pMap.get("lastEventDetail");
                    int newPos = ((Number) pMap.get("lastEventPos")).intValue();
                    p.setLastEvent(new model.board.EventManager.EventResult(type, detail, newPos));
                }
                
                players.add(p);
            }
            model.config.GameSetupConfig.setPlayers(players);

            if (state.containsKey("seal")) {
            	model.config.GameSetupConfig.setSealEnabled(true);
            	model.config.GameSetupConfig.setLoadedSealState((Map<String, Object>) state.get("seal"));
            } else {
            	model.config.GameSetupConfig.setSealEnabled(false);
            	model.config.GameSetupConfig.setLoadedSealState(null);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
	public static List<String> getAllSavedGameIds() {
		// TODO Auto-generated method stub
		return null;
	}
}
