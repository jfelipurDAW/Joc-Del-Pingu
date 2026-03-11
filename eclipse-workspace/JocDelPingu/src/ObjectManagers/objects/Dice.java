package ObjectManagers.objects;

import ObjectManagers.ObjectType;

public class Dice extends Object{

	private static final int SLOWDICE_MIN_VALUE = 1;
	private static final int SLOWDICE_MAX_VALUE = 3;

	private static final int FASTDICE_MIN_VALUE = 5;
	private static final int FASTDICE_MAX_VALUE = 10;

	
	protected ObjectType objectType = ObjectType.DICE;
	
	protected int minValue;
    protected int maxValue;
    protected ObjectType diceType;
    
    public Dice(ObjectType diceType) {
        this.diceType = diceType;
        
        switch (diceType) {
        
        case FASTDICE:
        	
        	this.minValue = FASTDICE_MIN_VALUE;
        	this.maxValue = FASTDICE_MAX_VALUE;
        	
        	break;
    
        case SLOWDICE:
        	
        	this.minValue = SLOWDICE_MIN_VALUE;
        	this.maxValue = SLOWDICE_MAX_VALUE;
        	
        	break;
        
        }
    }
    
    public int roll() {
        return (int) ((Math.random() * (maxValue - minValue + 1)) + minValue);
    }
    
    public int getMinValue() { 
    	return minValue; 
    	}
    public int getMaxValue() { 
    	return maxValue; 
    	}
    
    public ObjectType getDiceType() {
    	return this.diceType;
    }
}
