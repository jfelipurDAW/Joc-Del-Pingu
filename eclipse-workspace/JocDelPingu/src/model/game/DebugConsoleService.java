package model.game;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

import controller.ui.GameBoardController;
import model.entity.Entity;
import model.entity.Player;
import model.entity.Seal;
import model.item.Inventory;
import model.item.ObjectType;
import model.board.Board;


/**
 * Singleton state holder + command parser for the in-game debug console.
 *
 * <p>The console is implemented as a separate modeless JavaFX Stage so it can
 * be opened from the main menu or while a game is running, and closing it
 * never affects gameplay. This service is the bridge between that window and
 * whichever {@link GameBoardController} happens to be alive at the moment.</p>
 *
 * <p>Commands accepted are Minecraft-style (slash-prefixed):</p>
 * <ul>
 *   <li>{@code /help}                          - list available commands</li>
 *   <li>{@code /list}                          - list players and seal</li>
 *   <li>{@code /tp <name|seal> <square>}       - teleport to a square index</li>
 *   <li>{@code /view numbers}                  - toggle square-number overlay</li>
 *   <li>{@code /give <player> <item> <amt>}    - add items to inventory</li>
 *   <li>{@code /clear <player>}                - empty a player's inventory</li>
 *   <li>{@code /setdice <value>}               - force the next dice result</li>
 *   <li>{@code /reset}                         - clear the forced dice value</li>
 * </ul>
 */
public class DebugConsoleService {


    /////////////////////////////
    ///       SINGLETON       ///
    /////////////////////////////

    private static final DebugConsoleService INSTANCE = new DebugConsoleService();

    public static DebugConsoleService getInstance() { return INSTANCE; }

    private DebugConsoleService() {}


    /////////////////////////////
    ///        STATE          ///
    /////////////////////////////

    // The currently-visible game board controller (or null if we are in the
    // main menu / player setup / stats screens). Most commands only make
    // sense while a game is running, so they short-circuit when this is null.
    private GameBoardController activeController;

    // Global toggle for the "show square numbers" overlay. Lives here (not
    // in the controller) so the setting survives between game sessions and
    // is visible to the console even when no game is running.
    private boolean viewNumbers = false;

    // Optional console output sink — when the debug window is open, it
    // registers itself here so background messages (e.g. the controller
    // reporting a debug event) can be echoed into the console log too.
    private java.util.function.Consumer<String> outputSink;


    /////////////////////////////
    ///  ACTIVE CONTROLLER    ///
    /////////////////////////////

    /**
     * Registers the game board that is currently on screen. Pass {@code null}
     * when the board is being torn down (navigating back to the main menu).
     * Calling this re-applies the global {@link #isViewNumbers()} flag to the
     * new controller so the number overlay state is consistent.
     */
    public void setActiveBoardController(GameBoardController c) {
        this.activeController = c;
        if (c != null) {
            c.applyDebugViewNumbers(viewNumbers);
        }
    }

    public GameBoardController getActiveBoardController() { return activeController; }


    /////////////////////////////
    ///     OUTPUT SINK       ///
    /////////////////////////////

    public void setOutputSink(java.util.function.Consumer<String> sink) {
        this.outputSink = sink;
    }

    /** Push a line into the console log if it's open; ignored otherwise. */
    public void log(String line) {
        java.util.function.Consumer<String> sink = outputSink;
        if (sink != null) {
            Platform.runLater(() -> sink.accept(line));
        }
    }


    /////////////////////////////
    ///     VIEW NUMBERS      ///
    /////////////////////////////

    public boolean isViewNumbers() { return viewNumbers; }


    /////////////////////////////
    ///   COMMAND ENTRYPOINT  ///
    /////////////////////////////

    /**
     * Parses and executes one command line. Returns a single-string response
     * that the console UI is expected to print into its log area. Never
     * returns {@code null}.
     */
    public String executeCommand(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return "";
        if (!trimmed.startsWith("/")) {
            return "Commands must start with '/'. Type /help for a list.";
        }

        String[] parts = trimmed.split("\\s+");
        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "/help":     return helpText();
                case "/list":     return listEntities();
                case "/tp":       return doTeleport(parts);
                case "/view":     return doView(parts);
                case "/give":     return doGive(parts);
                case "/clear":    return doClear(parts);
                case "/setdice":  return doSetDice(parts);
                case "/reset":    return doResetDice();
                default:          return "Unknown command: " + cmd + ". Type /help.";
            }
        } catch (NumberFormatException nfe) {
            return "Invalid number: " + nfe.getMessage();
        } catch (Exception ex) {
            return "ERROR: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
    }


    /////////////////////////////
    ///    /help              ///
    /////////////////////////////

    private String helpText() {
        return String.join("\n",
            "Available commands:",
            "  /help                              - show this help",
            "  /list                              - list players and the seal",
            "  /tp <name|seal> <square>           - teleport a target to a square (0-" + (Board.MAX_SQUARES - 1) + ")",
            "  /view numbers                      - toggle square number overlay",
            "  /give <player> <item> <amount>     - add items (snowball, fish, fastdice, slowdice)",
            "  /clear <player>                    - empty a player's inventory",
            "  /setdice <value>                   - force the next dice roll (1-6)",
            "  /reset                             - clear the forced dice value"
        );
    }


    /////////////////////////////
    ///    /list              ///
    /////////////////////////////

    private String listEntities() {
        GameBoardController gbc = activeController;
        if (gbc == null) return "No active game.";
        TurnController tc = gbc.getTurnController();
        if (tc == null) return "No active game.";

        StringBuilder sb = new StringBuilder("Entities on board:\n");
        List<Entity> all = tc.getAllPlayers();
        for (Entity e : all) {
            if (e instanceof Player) {
                Player p = (Player) e;
                Inventory inv = p.getInventory();
                sb.append("  [").append(p.getSquareIndex()).append("] ").append(p.getName())
                  .append(" -  snow=").append(inv.getSnowballQuantity())
                  .append("  fish=").append(inv.getFishQuantity())
                  .append("  fast=").append(inv.getFastdiceQuantity())
                  .append("  slow=").append(inv.getSlowdiceQuantity())
                  .append("\n");
            }
        }
        Seal seal = gbc.getSeal();
        if (seal != null && gbc.isSealEnabled()) {
            sb.append("  [").append(seal.getSquareIndex()).append("] seal\n");
        } else {
            sb.append("  (seal disabled)\n");
        }
        return sb.toString().stripTrailing();
    }


    /////////////////////////////
    ///    /tp                ///
    /////////////////////////////

    private String doTeleport(String[] parts) {
        if (parts.length != 3) return "Usage: /tp <name|seal> <square>";
        GameBoardController gbc = activeController;
        if (gbc == null) return "No active game.";

        String target = parts[1];
        int square    = Integer.parseInt(parts[2]);
        if (square < 0 || square >= Board.MAX_SQUARES) {
            return "Square out of range. Valid: 0-" + (Board.MAX_SQUARES - 1) + ".";
        }

        if (target.equalsIgnoreCase("seal")) {
            Seal seal = gbc.getSeal();
            if (seal == null || !gbc.isSealEnabled()) {
                return "Seal is disabled in this game.";
            }
            seal.setSquare(square);
            gbc.requestRedraw();
            return "Teleported seal to square " + square + ".";
        }

        Player p = findPlayer(gbc, target);
        if (p == null) return "No player named '" + target + "'.";
        p.setSquare(square);
        gbc.requestRedraw();
        return "Teleported " + p.getName() + " to square " + square + ".";
    }


    /////////////////////////////
    ///    /view              ///
    /////////////////////////////

    private String doView(String[] parts) {
        if (parts.length != 2) return "Usage: /view numbers";
        String what = parts[1].toLowerCase();
        if (!what.equals("numbers")) {
            return "Unknown view: " + parts[1] + ". Try /view numbers.";
        }
        viewNumbers = !viewNumbers;
        if (activeController != null) activeController.applyDebugViewNumbers(viewNumbers);
        return "Square numbers " + (viewNumbers ? "ENABLED." : "disabled.");
    }


    /////////////////////////////
    ///    /give              ///
    /////////////////////////////

    private String doGive(String[] parts) {
        if (parts.length != 4) return "Usage: /give <player> <item> <amount>";
        GameBoardController gbc = activeController;
        if (gbc == null) return "No active game.";

        String playerName = parts[1];
        String itemRaw    = parts[2].toLowerCase();
        int amount        = Integer.parseInt(parts[3]);

        if (amount <= 0) return "Amount must be a positive integer.";

        Player p = findPlayer(gbc, playerName);
        if (p == null) return "No player named '" + playerName + "'.";

        Inventory inv = p.getInventory();
        int actual = 0;
        switch (itemRaw) {
            case "snowball":
            case "snowballs":
                actual = inv.addSnowballs(amount);
                break;
            case "fish":
                for (int i = 0; i < amount; i++) {
                    if (inv.addFish()) actual++;
                }
                break;
            case "fastdice":
                for (int i = 0; i < amount; i++) {
                    if (inv.addDice(ObjectType.FASTDICE)) actual++;
                }
                break;
            case "slowdice":
                for (int i = 0; i < amount; i++) {
                    if (inv.addDice(ObjectType.SLOWDICE)) actual++;
                }
                break;
            default:
                return "Unknown item: " + itemRaw + ". Allowed: snowball, fish, fastdice, slowdice.";
        }
        gbc.requestRedraw();
        if (actual == amount) {
            return "Gave " + amount + " [" + itemRaw + "] to " + p.getName() + ".";
        }
        return "Gave " + actual + " [" + itemRaw + "] to " + p.getName()
                + " (" + (amount - actual) + " refused: inventory full).";
    }


    /////////////////////////////
    ///    /clear             ///
    /////////////////////////////

    private String doClear(String[] parts) {
        if (parts.length != 2) return "Usage: /clear <player>";
        GameBoardController gbc = activeController;
        if (gbc == null) return "No active game.";

        Player p = findPlayer(gbc, parts[1]);
        if (p == null) return "No player named '" + parts[1] + "'.";

        Inventory inv = p.getInventory();
        inv.setSnowballQuantity(0);
        inv.setFishQuantity(0);
        inv.setFastdiceQuantity(0);
        inv.setSlowdiceQuantity(0);
        inv.setDiceQuantity(0);
        gbc.requestRedraw();
        return "Cleared inventory of " + p.getName() + ".";
    }


    /////////////////////////////
    ///    /setdice           ///
    /////////////////////////////

    private String doSetDice(String[] parts) {
        if (parts.length != 2) return "Usage: /setdice <value>";
        GameBoardController gbc = activeController;
        if (gbc == null) return "No active game.";
        int value = Integer.parseInt(parts[1]);
        if (value < 1 || value > 6) return "Dice value must be between 1 and 6.";
        gbc.setDebugForcedDice(value);
        return "Next dice roll will be " + value + ".";
    }


    /////////////////////////////
    ///    /reset             ///
    /////////////////////////////

    private String doResetDice() {
        GameBoardController gbc = activeController;
        if (gbc == null) return "No active game.";
        gbc.setDebugForcedDice(null);
        return "Forced dice cleared.";
    }


    /////////////////////////////
    ///      HELPERS          ///
    /////////////////////////////

    private Player findPlayer(GameBoardController gbc, String name) {
        TurnController tc = gbc.getTurnController();
        if (tc == null) return null;
        for (Entity e : tc.getAllPlayers()) {
            if (e instanceof Player && e.getName().equalsIgnoreCase(name)) {
                return (Player) e;
            }
        }
        return null;
    }
}
