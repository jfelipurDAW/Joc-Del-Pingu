package model.config;


/**
 * Enumeration of every translatable text key consumed by the UI.
 *
 * <p>Each constant carries the dotted path used inside the localisation YAML
 * files (e.g. {@code /assets/lang/en.yml}). Centralising these identifiers in
 * a single enum means the compiler — rather than runtime string comparison —
 * catches typos, and refactoring tools can find every usage of a given key.</p>
 *
 * <p>At runtime, {@link LangConfig#getLang(Lang)} maps a constant to the
 * translated string for the currently loaded language.</p>
 *
 * <p>The constants are grouped by feature area (entities, in-game objects,
 * main menu, setup screen, stats, alerts, dialogs) for readability.</p>
 */
public enum Lang {


    /////////////////////////////
    ///   ENTITY LABELS       ///
    /////////////////////////////

    ENTITY_SEAL     ("entity.seal"),
    ENTITY_PLAYER   ("entity.player"),


    /////////////////////////////
    ///   IN-GAME OBJECTS     ///
    /////////////////////////////

    OBJECT_SNOWBALL ("object.snowball"),
    OBJECT_FISH     ("object.fish"),
    OBJECT_DICE     ("object.dice"),

    // Fixed keys to match YAML structure (object.dice.fast / object.dice.slow)
    OBJECT_FASTDICE ("object.dice.fast"),
    OBJECT_SLOWDICE ("object.dice.slow"),


    /////////////////////////////
    ///   MAIN MENU BUTTONS   ///
    /////////////////////////////

    MENU_BUTTON_NEWGAME ("menu.button.newgame"),
    MENU_BUTTON_LOADGAME ("menu.button.loadgame"),
    MENU_BUTTON_LANGUAGE ("menu.button.language"),


    /////////////////////////////
    ///   GAME / SETUP TEXT   ///
    /////////////////////////////

    TEXT_GAME_TITLE ("text.game.title"),

    TEXT_SETUP_TITLE ("text.game.setup"),

    // Missing key that exists in the YAML files
    TEXT_GAME_STARTGAME ("text.game.startgame"),

    GAMESETUP_TEXT_PLAYERNUMBER ("menu.gamesetup.playernumber"),
    GAMESETUP_TEXT_ENABLESEAL ("menu.gamesetup.enableseal"),

    GAMESETUP_PLAYER ("gamesetup.player"),
    GAMESETUP_PLAYERNAME ("gamesetup.playername"),
    GAMESETUP_PASSWORD ("gamesetup.password"),


    /////////////////////////////
    ///   STATS SCREEN        ///
    /////////////////////////////

    MENU_BUTTON_STATS ("menu.button.stats"),
    STATS_TITLE ("stats.title"),
    STATS_PLAYER ("stats.player"),
    STATS_COLOUR ("stats.colour"),
    STATS_GAMES_PLAYED ("stats.gamesplayed"),
    STATS_GAMES_WON ("stats.gameswon"),
    STATS_NO_DATA ("stats.nodata"),
    GAME_BACK_TO_MENU ("game.backtomenu"),


    /////////////////////////////
    ///   SETUP BUTTONS       ///
    /////////////////////////////

    GAMESETUP_BUTTON_BACK           ("gamesetup.button.back"),
    GAMESETUP_BUTTON_SELECTPLAYER   ("gamesetup.button.selectplayer"),
    GAMESETUP_CHOOSEAVATAR          ("gamesetup.chooseavatar"),
    GAMESETUP_AVATARSELECTED        ("gamesetup.avatarselected"),
    GAMESETUP_FILECHOOSER_TITLE     ("gamesetup.filechooser.title"),


    /////////////////////////////
    ///   ALERT MESSAGES      ///
    /////////////////////////////

    ALERT_WRONGPASSWORD_TITLE   ("alert.wrongpassword.title"),
    ALERT_WRONGPASSWORD_MESSAGE ("alert.wrongpassword.message"),
    ALERT_NOPLAYERS_TITLE       ("alert.noplayers.title"),
    ALERT_NOPLAYERS_MESSAGE     ("alert.noplayers.message"),
    ALERT_FULL_TITLE            ("alert.full.title"),
    ALERT_FULL_MESSAGE          ("alert.full.message"),


    /////////////////////////////
    ///   DIALOGS             ///
    /////////////////////////////

    DIALOG_SELECTPLAYER_TITLE  ("dialog.selectplayer.title"),
    DIALOG_SELECTPLAYER_HEADER ("dialog.selectplayer.header");


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    // The dotted path that locates this entry inside the YAML translation files.
    private final String key;


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Binds an enum constant to its dotted YAML key.
     *
     * @param key the path used to look up the value inside the translation YAML
     */
    Lang(String key) {
        this.key = key;
    }


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    /**
     * @return the dotted YAML key associated with this constant; used by
     *         {@link LangConfig#getLang(Lang)} to look up the translation
     */
    public String getKey() {
        return key;
    }
}
