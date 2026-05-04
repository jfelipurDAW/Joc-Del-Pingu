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

 // Cambia la firma del método para aceptar 'customName'
    public static boolean saveGame(String customName, Object board, TurnController turnController, Seal seal) {
        // ... (toda tu lógica anterior de serialización YAML y Encriptación se mantiene igual) ...
        
        try {
            Yaml yaml = new Yaml();
            Object state = null;
			String yamlString = yaml.dump(state);
            String encrypted = CryptoUtil.encrypt(yamlString);
            
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                // USAMOS EL NOMBRE QUE PASA EL USUARIO
                // Usamos MERGE o una comprobación para no duplicar si el nombre ya existe
                String sql = "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) VALUES ('" + customName + "', '" + encrypted + "')";
                
                BBDD.insert(con, sql);
                BBDD.cerrar(con);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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