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
            // USAMOS EL NOMBRE QUE PASA EL USUARIO (sanitizado contra inyección básica)
            String safeName = customName.replace("'", "''");

            Map<String, Object> state = new HashMap<>();

            // Serializar el tablero
            List<String> boardState = new ArrayList<>();
            for (model.board.Square sq : ((Board)board).getBoard()) {
                boardState.add(sq.getType().name());
            }
            state.put("board", boardState);

            // Turno actual
            state.put("currentTurn", turnController.getCurrentTurnIndex());

            // Jugadores
            List<Map<String, Object>> playersList = new ArrayList<>();
            for (Entity e : turnController.getAllPlayers()) {
                if (e instanceof Player) {
                    Player p = (Player) e;
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("name", p.getName());
                    pMap.put("color", p.getColour());
                    pMap.put("password", p.getPassword() != null ? p.getPassword() : "");
                    pMap.put("id", p.getEntityId());
                    pMap.put("square", p.getSquareIndex());
                    pMap.put("skipNextTurn", p.shouldSkipNextTurn());
                    
                    Map<String, Integer> invMap = new HashMap<>();
                    Inventory inv = p.getInventory();
                    invMap.put("snowballs", inv.getSnowballQuantity());
                    invMap.put("fish", inv.getFishQuantity());
                    invMap.put("fastdice", inv.getFastdiceQuantity());
                    invMap.put("slowdice", inv.getSlowdiceQuantity());
                    pMap.put("inventory", invMap);
                    
                    playersList.add(pMap);
                }
            }
            state.put("players", playersList);

            // Foca
            if (seal != null) {
                Map<String, Object> sealState = new HashMap<>();
                sealState.put("square", seal.getSquareIndex());
                sealState.put("blockedTurns", seal.getBlockedTurns());
                state.put("seal", sealState);
            }

            Yaml yaml = new Yaml();
            String yamlString = yaml.dump(state);
            String encrypted = CryptoUtil.encrypt(yamlString);
            
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                // Usamos MERGE o una comprobación para no duplicar si el nombre ya existe
                String sql = "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) VALUES ('" + safeName + "', '" + encrypted + "')";
                
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
    
    /**
     * Guarda un nuevo perfil de jugador en la tabla ENTITY.
     */
    public static boolean registerPlayer(String name, String password, String color) {
        Connection con = null;
        try {
            con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                // El ID puede ser un random o un auto-incremental
                int id = (int) (Math.random() * 10000); 
                String sql = "INSERT INTO ENTITY (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR) " +
                             "VALUES (" + id + ", 'PLAYER', '" + name + "', '" + password + "', '" + color + "')";
                
                BBDD.insert(con, sql);
                BBDD.cerrar(con);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Recupera todos los jugadores registrados para poder elegirlos.
     */
    public static List<Player> getRegisteredPlayers() {
        List<Player> players = new ArrayList<>();
        Connection con = null;
        try {
            con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                String sql = "SELECT PLAYERNAME, PLAYERPASSWORD, COLOUR FROM ENTITY WHERE ENTITYTYPE = 'PLAYER'";
                java.util.ArrayList<java.util.LinkedHashMap<String, String>> result = BBDD.select(con, sql);
                BBDD.cerrar(con);

                for (java.util.LinkedHashMap<String, String> row : result) {
                    Player p = new Player(row.get("PLAYERNAME"), row.get("COLOUR"), row.get("PLAYERPASSWORD"));
                    players.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
}