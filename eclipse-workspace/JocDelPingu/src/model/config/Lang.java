package model.config;


/////////////////////////////////////////////////
///    STRING_KEYS'S TO REFEER THE LANG.YML   /// 
/////////////////////////////////////////////////

public enum Lang {
	
    ENTITY_SEAL     ("entity.seal"),
    ENTITY_PLAYER   ("entity.player"),
    
    OBJECT_SNOWBALL ("object.snowball"),
    OBJECT_FISH     ("object.fish"),
    OBJECT_DICE     ("object.dice"),
    // Fixed keys to match YAML structure (object.dice.fast / object.dice.slow)
    OBJECT_FASTDICE ("object.dice.fast"),
    OBJECT_SLOWDICE ("object.dice.slow"),
	
	MENU_BUTTON_NEWGAME ("menu.button.newgame"),
	MENU_BUTTON_LOADGAME ("menu.button.loadgame"),
	MENU_BUTTON_LANGUAGE ("menu.button.language"),
	
	TEXT_GAME_TITLE ("text.game.title"), 
	
	TEXT_SETUP_TITLE ("text.game.setup"), 
	
	// Missing key that exists in the YAML files
	TEXT_GAME_STARTGAME ("text.game.startgame"),
	
	GAMESETUP_TEXT_PLAYERNUMBER ("menu.gamesetup.playernumber"), 
	GAMESETUP_TEXT_ENABLESEAL ("menu.gamesetup.enableseal"),

	GAMESETUP_PLAYER ("gamesetup.player"),
	GAMESETUP_PLAYERNAME ("gamesetup.playername"),
	GAMESETUP_PASSWORD ("gamesetup.password"),

	MENU_BUTTON_STATS ("menu.button.stats"),
	STATS_TITLE ("stats.title"),
	STATS_PLAYER ("stats.player"),
	STATS_COLOUR ("stats.colour"),
	STATS_GAMES_PLAYED ("stats.gamesplayed"),
	STATS_GAMES_WON ("stats.gameswon"),
	STATS_NO_DATA ("stats.nodata"),
	GAME_BACK_TO_MENU ("game.backtomenu");

    private final String key;

    Lang(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}