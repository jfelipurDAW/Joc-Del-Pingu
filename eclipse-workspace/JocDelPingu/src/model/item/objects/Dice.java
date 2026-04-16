package model.item.objects;

import model.item.ObjectType;

public class Dice extends model.item.GameObject {

	private static final int SLOWDICE_MIN_VALUE = 1;
	private static final int SLOWDICE_MAX_VALUE = 3;

	private static final int FASTDICE_MIN_VALUE = 5;
	private static final int FASTDICE_MAX_VALUE = 10;

	protected int minValue;
    protected int maxValue;
    protected ObjectType diceType;
    
    public Dice(ObjectType diceType) {
    	super(diceType);
        this.diceType = diceType;
        
        switch (diceType) {
        
        case FASTDICE:
        	this.name = "Fast Dice";
        	this.minValue = FASTDICE_MIN_VALUE;
        	this.maxValue = FASTDICE_MAX_VALUE;
        	break;
    
        case SLOWDICE:
        	this.name = "Slow Dice";
        	this.minValue = SLOWDICE_MIN_VALUE;
        	this.maxValue = SLOWDICE_MAX_VALUE;
        	break;
        	
        default:
        	this.name = "Dice";
        	this.minValue = 1;
        	this.maxValue = 6;
        	break;
        }
    }
    
    public int roll() {
        return (int) ((java.lang.Math.random() * (maxValue - minValue + 1)) + minValue);    
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
