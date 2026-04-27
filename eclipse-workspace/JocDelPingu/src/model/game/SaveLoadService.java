package model.game;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import model.item.Inventory;
import model.board.Board;
import model.config.CryptoUtil;
import model.entity.Entity;
import model.entity.Player;
import model.entity.Seal;
import model.db.BBDD;

public class SaveLoadService {

    /**
     * Recupera todos los IDs de las partidas guardadas para el selector.
     */
    public static List<String> getAllSavedGameIds() {
        List<String> ids = new ArrayList<>();
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                // Seleccionamos todos los IDs de la tabla
                String sql = "SELECT GAME_ID FROM SAVED_GAMES ORDER BY GAME_ID DESC";
                java.util.ArrayList<java.util.LinkedHashMap<String, String>> result = BBDD.select(con, sql);
                BBDD.cerrar(con);

                for (java.util.LinkedHashMap<String, String> row : result) {
                    ids.add(row.get("GAME_ID"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

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
            Yaml yaml = new Yaml();
            String yamlString = yaml.dump(state);
            String encrypted = CryptoUtil.encrypt(yamlString);
            
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                String idUnico = "PARTIDA_" + System.currentTimeMillis();
                BBDD.insert(con, "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) VALUES ('" + idUnico + "', '" + encrypted + "')");
                BBDD.cerrar(con);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadGame(String gameId) {
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con == null) return false;

            java.util.ArrayList<java.util.LinkedHashMap<String, String>> result = BBDD.select(con, "SELECT GAME_DATA FROM SAVED_GAMES WHERE GAME_ID = '" + gameId + "'");
            BBDD.cerrar(con);

            if (result.isEmpty()) return false;

            String encrypted = result.get(0).get("GAME_DATA");
            String yamlString = CryptoUtil.decrypt(encrypted);
            if (yamlString == null) return false;

            Yaml yaml = new Yaml();
            Map<String, Object> state = yaml.load(yamlString);

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
                
                Map<String, Integer> invMap = (Map<String, Integer>) pMap.get("inventory");
                Inventory inv = p.getInventory();
                inv.setSnowballQuantity(invMap.get("snowballs"));
                inv.setFishQuantity(invMap.get("fish"));
                inv.setFastdiceQuantity(invMap.get("fastdice"));
                inv.setSlowdiceQuantity(invMap.get("slowdice"));
                inv.setDiceQuantity(invMap.get("fastdice") + invMap.get("slowdice"));
                players.add(p);
            }
            model.config.GameSetupConfig.setPlayers(players);

            if (state.containsKey("seal")) {
                model.config.GameSetupConfig.setSealEnabled(true);
                model.config.GameSetupConfig.setLoadedSealState((Map<String, Object>) state.get("seal"));
            } else {
                model.config.GameSetupConfig.setSealEnabled(false);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}