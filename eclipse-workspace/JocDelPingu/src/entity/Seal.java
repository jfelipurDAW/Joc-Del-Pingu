package entity;

import config.Lang;
import config.LangConfig;

public class Seal extends Entity{

	public Seal() {
		this.name = Lang.ENTITY_SEAL.getKey();
		this.type = EntityType.PLAYER;		
		
	}
	
}
