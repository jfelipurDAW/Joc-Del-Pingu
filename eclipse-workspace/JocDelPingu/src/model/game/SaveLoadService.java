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
    public static boolean saveGame(String customName, Object board, TurnController turnController, Seal seal, String winner) {
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

    /**
     * Records the result of a completed game: inserts a GAME row and increments
     * GAMES_PLAYED for every player, plus GAMES_WON for the winner.
     * Pass null/empty winnerName when no player wins (e.g. seal victory).
     */
    public static void recordGameResult(java.util.List<Entity> allPlayers, String winnerName) {
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                BBDD.insert(con, "INSERT INTO GAME (GAMESTATE, GAMEDATE, BOARDID) VALUES ('FINISHED', SYSDATE, 1)");

                for (Entity e : allPlayers) {
                    if (e instanceof Player) {
                        String safeName = ((Player) e).getName().replace("'", "''");
                        BBDD.update(con, "UPDATE ENTITY SET GAMES_PLAYED = GAMES_PLAYED + 1 " +
                            "WHERE PLAYERNAME = '" + safeName + "' AND ENTITYTYPE = 'PLAYER'");
                    }
                }

                if (winnerName != null && !winnerName.isEmpty()) {
                    String safeWinner = winnerName.replace("'", "''");
                    BBDD.update(con, "UPDATE ENTITY SET GAMES_WON = GAMES_WON + 1 " +
                        "WHERE PLAYERNAME = '" + safeWinner + "' AND ENTITYTYPE = 'PLAYER'");
                }

                BBDD.cerrar(con);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            // Only mark as loaded game after ALL data has been successfully populated
            model.config.GameSetupConfig.setLoadedGame(true);
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
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                String safeName     = name.replace("'", "''");
                // Encrypt the password before storing
                String encryptedPwd = CryptoUtil.encrypt(password != null ? password : "");
                String safePassword = (encryptedPwd != null ? encryptedPwd : "").replace("'", "''");
                String safeColor    = (color != null ? color : "FFFFFF").replace("'", "''");
                // Derive the next ID from the current max so we stay within the column's precision.
                int newId = 1;
                java.util.ArrayList<java.util.LinkedHashMap<String, String>> maxResult =
                    BBDD.select(con, "SELECT NVL(MAX(ENTITYID), 0) + 1 AS NEXTID FROM ENTITY");
                if (!maxResult.isEmpty()) {
                    newId = Integer.parseInt(maxResult.get(0).get("NEXTID"));
                }

                // MERGE: actualiza si el nombre ya existe, inserta si es nuevo
                String sql =
                    "MERGE INTO ENTITY e " +
                    "USING (SELECT '" + safeName + "' AS pname FROM DUAL) src " +
                    "ON (e.PLAYERNAME = src.pname AND e.ENTITYTYPE = 'PLAYER') " +
                    "WHEN MATCHED THEN " +
                    "  UPDATE SET e.PLAYERPASSWORD = '" + safePassword + "', " +
                    "             e.COLOUR = '" + safeColor + "' " +
                    "WHEN NOT MATCHED THEN " +
                    "  INSERT (ENTITYID, ENTITYTYPE, PLAYERNAME, PLAYERPASSWORD, COLOUR, GAMES_PLAYED, GAMES_WON) " +
                    "  VALUES (" + newId + ", 'PLAYER', '" + safeName + "', '" +
                                   safePassword + "', '" + safeColor + "', 0, 0)";

                BBDD.executeInsUpDel(con, sql, "Merge");
                BBDD.cerrar(con);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifies a player's password against the encrypted value stored in the DB.
     * Returns true if the password matches, false otherwise.
     */
    public static boolean verifyPassword(String playerName, String inputPassword) {
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                String safeName = playerName.replace("'", "''");
                String sql = "SELECT PLAYERPASSWORD FROM ENTITY WHERE PLAYERNAME = '" + safeName + "' AND ENTITYTYPE = 'PLAYER'";
                java.util.ArrayList<java.util.LinkedHashMap<String, String>> result = BBDD.select(con, sql);
                BBDD.cerrar(con);

                if (!result.isEmpty()) {
                    String storedEncrypted = result.get(0).get("PLAYERPASSWORD");
                    if (storedEncrypted == null || storedEncrypted.isEmpty()) {
                        // No password set — allow if input is also empty
                        return (inputPassword == null || inputPassword.isEmpty());
                    }
                    String decrypted = CryptoUtil.decrypt(storedEncrypted);
                    return inputPassword != null && inputPassword.equals(decrypted);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Player not found in DB — this is a new player, allow
        return true;
    }

    /**
     * Retrieves player statistics from the database.
     * Returns a list of maps with keys: PLAYERNAME, COLOUR, GAMES_PLAYED, GAMES_WON.
     */
    public static java.util.ArrayList<java.util.LinkedHashMap<String, String>> getPlayerStats() {
        java.util.ArrayList<java.util.LinkedHashMap<String, String>> stats = new java.util.ArrayList<>();
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                String sql = "SELECT PLAYERNAME, COLOUR, " +
                             "NVL(GAMES_PLAYED, 0) AS GAMES_PLAYED, " +
                             "NVL(GAMES_WON, 0) AS GAMES_WON " +
                             "FROM ENTITY WHERE ENTITYTYPE = 'PLAYER' " +
                             "ORDER BY GAMES_WON DESC, GAMES_PLAYED DESC";
                stats = BBDD.select(con, sql);
                BBDD.cerrar(con);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Recupera todos los jugadores registrados para poder elegirlos.
     * Passwords are decrypted from the DB for in-memory use.
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
                    String encPwd = row.get("PLAYERPASSWORD");
                    String decPwd = (encPwd != null && !encPwd.isEmpty()) ? CryptoUtil.decrypt(encPwd) : "";
                    Player p = new Player(row.get("PLAYERNAME"), row.get("COLOUR"), decPwd);
                    players.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
}