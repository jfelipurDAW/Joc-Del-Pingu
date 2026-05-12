package model.game;

import java.sql.Connection;
import java.sql.PreparedStatement;
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


/**
 * Static facade for all persistence operations of "Joc del Pingu".
 *
 * Two distinct sets of responsibilities live here:
 *
 *  1. Save / load of an in-progress match:
 *     The full {@link Game} state (board layout, current turn, every
 *     player's position + inventory + event history, and the optional seal)
 *     is dumped to YAML via SnakeYAML, AES-encrypted by {@link CryptoUtil}
 *     and stored as a CLOB in the SAVED_GAMES Oracle table through the
 *     {@link BBDD} helper.
 *
 *  2. Registered-player management:
 *     Profile rows in the ENTITY table (name, encrypted password, colour,
 *     games played/won) plus password verification on login and a stats
 *     query for the stats screen.
 *
 * All methods are static — the class is purely a namespace; there is no
 * instance state.
 */
public class SaveLoadService {


    /////////////////////////////
    ///    SAVE GAME LIST     ///
    /////////////////////////////

    /**
     * Retrieves every saved-game id for the picker dialog.
     *
     * @return list of saved-game IDs in descending order (most recent first)
     */
    public static List<String> getAllSavedGameIds() {
        List<String> ids = new ArrayList<>();
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                // Select every id from the table
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


    /////////////////////////////
    ///       SAVE GAME       ///
    /////////////////////////////

    /**
     * Serializes the full game state (board, turn index, players, optional
     * seal) to YAML, encrypts it with AES-128 and persists it under the
     * supplied custom name. Returns true on success.
     *
     * @param customName     user-chosen save name (becomes the GAME_ID)
     * @param board          the {@link Board} (typed as Object to keep the
     *                       public signature decoupled from internals)
     * @param turnController turn controller — read for the current index and
     *                       the player list
     * @param seal           optional seal state (null if the feature is off)
     * @param winner         reserved; not used by the current save flow
     * @return true if at least one row was inserted in SAVED_GAMES
     */
    public static boolean saveGame(String customName, Object board, TurnController turnController, Seal seal, String winner) {
        try {
            // Snapshot every piece of state we care about into a plain
            // Map<String,Object> tree — SnakeYAML serializes that directly.
            Map<String, Object> state = new HashMap<>();

            // Serialize the board
            List<String> boardState = new ArrayList<>();
            for (model.board.Square sq : ((Board)board).getBoard()) {
                boardState.add(sq.getType().name());
            }
            state.put("board", boardState);

            // Current turn index
            state.put("currentTurn", turnController.getCurrentTurnIndex());

            // Players
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

                    // Inventory broken out per object type so the YAML is
                    // human-readable / debuggable instead of an opaque blob.
                    Map<String, Integer> invMap = new HashMap<>();
                    Inventory inv = p.getInventory();
                    invMap.put("snowballs", inv.getSnowballQuantity());
                    invMap.put("fish", inv.getFishQuantity());
                    invMap.put("fastdice", inv.getFastdiceQuantity());
                    invMap.put("slowdice", inv.getSlowdiceQuantity());
                    pMap.put("inventory", invMap);

                    // Persist the per-player event history (spec: save game events)
                    pMap.put("eventHistory", new ArrayList<>(p.getEventHistory()));

                    playersList.add(pMap);
                }
            }
            state.put("players", playersList);

            // Seal
            if (seal != null) {
                Map<String, Object> sealState = new HashMap<>();
                sealState.put("square", seal.getSquareIndex());
                sealState.put("blockedTurns", seal.getBlockedTurns());
                state.put("seal", sealState);
            }

            // YAML dump → AES encryption: the on-disk/in-DB form is opaque,
            // but loadGame() can decrypt and parse back to the same tree.
            Yaml yaml = new Yaml();
            String yamlString = yaml.dump(state);
            String encrypted = CryptoUtil.encrypt(yamlString);

            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                // GAME_DATA is a CLOB and the encrypted YAML easily exceeds
                // Oracle's 4000-char SQL-literal limit, so a concatenated
                // INSERT silently fails with ORA-01704. PreparedStatement
                // streams the value via JDBC and also avoids quote-escaping
                // issues in the encrypted blob and the user-supplied name.
                String sql = "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, customName);
                    ps.setString(2, encrypted);
                    int rows = ps.executeUpdate();

                    // Some JDBC configurations leave autocommit off — commit
                    // explicitly so the row survives the connection close.
                    if (!con.getAutoCommit()) {
                        con.commit();
                    }
                    BBDD.cerrar(con);
                    return rows > 0;
                } catch (java.sql.SQLException sqlEx) {
                    System.err.println("saveGame INSERT failed: " + sqlEx.getMessage());
                    BBDD.cerrar(con);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /////////////////////////////
    ///   GAME RESULT LOG     ///
    /////////////////////////////

    /**
     * Records the result of a completed game: inserts a GAME row and increments
     * GAMES_PLAYED for every player, plus GAMES_WON for the winner.
     * Pass null/empty winnerName when no player wins (e.g. seal victory).
     *
     * @param allPlayers  every participant of the finished match
     * @param winnerName  name of the winning player, or null/"" if none
     */
    public static void recordGameResult(java.util.List<Entity> allPlayers, String winnerName) {
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con != null) {
                // Ensure the BOARD row referenced by GAME.BOARDID exists.
                // Idempotent: only inserts if missing (avoids FK_GAME_BOARD violation).
                BBDD.executeInsUpDel(con,
                    "INSERT INTO BOARD (BOARDID) " +
                    "SELECT 1 FROM DUAL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM BOARD WHERE BOARDID = 1)",
                    "Insert BOARD");

                BBDD.insert(con, "INSERT INTO GAME (GAMESTATE, GAMEDATE, BOARDID) VALUES ('FINISHED', SYSDATE, 1)");

                // NVL guards against legacy rows where GAMES_PLAYED/GAMES_WON are NULL
                // (NULL + 1 = NULL in Oracle, which would silently keep counters at NULL).
                for (Entity e : allPlayers) {
                    if (e instanceof Player) {
                        String safeName = ((Player) e).getName().replace("'", "''");
                        int rows = BBDD.update(con, "UPDATE ENTITY SET GAMES_PLAYED = NVL(GAMES_PLAYED, 0) + 1 " +
                            "WHERE PLAYERNAME = '" + safeName + "' AND ENTITYTYPE = 'PLAYER'");
                        if (rows == 0) {
                            System.err.println("recordGameResult: no row updated for player '" + safeName + "'. " +
                                "Was the player registered in ENTITY?");
                        }
                    }
                }

                if (winnerName != null && !winnerName.isEmpty()) {
                    String safeWinner = winnerName.replace("'", "''");
                    int rows = BBDD.update(con, "UPDATE ENTITY SET GAMES_WON = NVL(GAMES_WON, 0) + 1 " +
                        "WHERE PLAYERNAME = '" + safeWinner + "' AND ENTITYTYPE = 'PLAYER'");
                    if (rows == 0) {
                        System.err.println("recordGameResult: no row updated for winner '" + safeWinner + "'.");
                    }
                }

                BBDD.cerrar(con);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /////////////////////////////
    ///       LOAD GAME       ///
    /////////////////////////////

    /**
     * Reverses {@link #saveGame}: fetches the encrypted CLOB by game id,
     * decrypts it, parses the YAML back into a tree, and pushes the
     * reconstructed values into {@link model.config.GameSetupConfig} so the
     * UI can rebuild the runtime objects from there.
     *
     * @param gameId save name used at save time
     * @return true if the saved state was successfully decoded and staged
     */
    public static boolean loadGame(String gameId) {
        try {
            Connection con = BBDD.conectarBaseDatos(null);
            if (con == null) return false;

            java.util.ArrayList<java.util.LinkedHashMap<String, String>> result = BBDD.select(con, "SELECT GAME_DATA FROM SAVED_GAMES WHERE GAME_ID = '" + gameId + "'");
            BBDD.cerrar(con);

            if (result.isEmpty()) return false;

            // AES roundtrip: encrypted CLOB -> YAML string -> Map tree.
            // A null here means decryption failed (corrupt blob / wrong key),
            // in which case we cannot trust the rest of the data.
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

                // Rebuild the inventory counts. The total "dice" count is
                // derived from fastdice + slowdice (they are stored
                // separately for clarity but tracked together at runtime).
                Map<String, Integer> invMap = (Map<String, Integer>) pMap.get("inventory");
                Inventory inv = p.getInventory();
                inv.setSnowballQuantity(invMap.get("snowballs"));
                inv.setFishQuantity(invMap.get("fish"));
                inv.setFastdiceQuantity(invMap.get("fastdice"));
                inv.setSlowdiceQuantity(invMap.get("slowdice"));
                inv.setDiceQuantity(invMap.get("fastdice") + invMap.get("slowdice"));

                Object historyObj = pMap.get("eventHistory");
                if (historyObj instanceof List) {
                    p.setEventHistory((List<String>) historyObj);
                }
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


    /////////////////////////////
    ///  PLAYER REGISTRATION  ///
    /////////////////////////////

    /**
     * Saves a new player profile in the ENTITY table.
     *
     * Uses MERGE so the call is idempotent: re-registering an existing name
     * just updates their password / colour instead of failing. The password
     * is AES-encrypted before it ever touches the DB.
     *
     * @param name     player display name (also the primary key)
     * @param password plain-text password (encrypted before insert)
     * @param color    preferred player colour (hex string, defaults to white)
     * @return true if the MERGE statement executed without an exception
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

                // MERGE: updates the row if the name already exists, otherwise inserts a new one
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
     *
     * Special cases:
     *  - if the DB has no row for this name it is treated as a brand-new
     *    player and access is allowed,
     *  - if the stored password is empty, login is allowed only when the
     *    input is also empty.
     *
     * @param playerName    name to look up
     * @param inputPassword password typed by the user
     * @return true if the credentials are acceptable
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

                    // Decrypt the stored value and do a plain string compare —
                    // the encryption is for at-rest protection, not hashing.
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


    /////////////////////////////
    ///   PLAYER STATS / LIST ///
    /////////////////////////////

    /**
     * Retrieves player statistics from the database.
     * Returns a list of maps with keys: PLAYERNAME, COLOUR, GAMES_PLAYED, GAMES_WON.
     *
     * Sorted by wins (then by games played) so the stats screen can render
     * a natural leaderboard.
     *
     * @return ordered list of stat rows, one per registered player
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
     * Retrieves every registered player so they can be picked from the list.
     * Passwords are decrypted from the DB for in-memory use.
     *
     * @return list of {@link Player} objects with their stored colour and
     *         decrypted password populated
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
                    // Decrypt only if a password is actually stored; empty
                    // string keeps "no password" semantics for new players.
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
