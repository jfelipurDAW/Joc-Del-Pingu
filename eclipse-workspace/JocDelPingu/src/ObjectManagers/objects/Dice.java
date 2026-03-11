package ObjectManagers.objects;

import ObjectManagers.ObjectType;

public class Dice extends Object{

	protected ObjectType objectType = ObjectType.DICE;
	
	protected int minValue;
    protected int maxValue;
    
    public Dice(ObjectType type, int min, int max) {
        super(type);
        this.minValue = min;
        this.maxValue = max;
    }
    
    public int roll() {
        return (int) (Math.random() * (maxValue - minValue + 1)) + minValue;
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
