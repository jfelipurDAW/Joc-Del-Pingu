package config;

public enum Lang {
	
    ENTITY_SEAL     ("object.dice"),
    ENTITY_PLAYER   ("entity.player"),
    
    OBJECT_SNOWBALL ("object.snowball"),
    OBJECT_FISH     ("object.fish"),
    OBJECT_DICE     ("object.dice"),
    OBJECT_FASTDICE ("object.fastdice"),
    OBJECT_SLOWDICE ("object.slowdice"),
	
	MENU_BUTTON_EXIT ("menu.button.exit");

    private final String key;

    Lang(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}