package entity;

import config.Lang;
import config.LangConfig;

public class Seal extends Entity{

	private boolean bribe;
	
	public Seal() {
		this.type = EntityType.PLAYER;		
		
	}
	
	@Override
	public String getName() {
		return Lang.ENTITY_SEAL.getKey();
	}
	
	public boolean hasBeenBribed() {
		return bribe;
	}
	
}
