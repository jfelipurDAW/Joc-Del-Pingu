package config;

public enum Lang {
	
    ENTITY_SEAL     ("entity.seal"),
    ENTITY_PLAYER   ("entity.player"),
    
    OBJECT_SNOWBALL ("object.snowball"),
    OBJECT_FISH     ("object.fish"),
    OBJECT_DICE     ("object.dice"),
    OBJECT_FASTDICE ("object.fastdice"),
    OBJECT_SLOWDICE ("object.slowdice"),
	
	MENU_BUTTON_NEWGAME ("menu.button.newgame"),
	MENU_BUTTON_LOADGAME ("menu.button.loadgame"),
	MENU_BUTTON_LANGUAGE ("menu.button.language");

    private final String key;

    Lang(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}