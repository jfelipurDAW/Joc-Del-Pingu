package model.item.objects;

import model.item.ObjectType;

public class Dice extends model.item.GameObject {

	private static final int SLOWDICE_MIN_VALUE = 1;
	private static final int SLOWDICE_MAX_VALUE = 3;

	private static final int FASTDICE_MIN_VALUE = 5;
	private static final int FASTDICE_MAX_VALUE = 10;

	private int minValue;
    private int maxValue;

    public Dice(ObjectType diceType) {
    	super(diceType);

        switch (diceType) {

        case FASTDICE:
        	this.setName("Fast Dice");
        	this.minValue = FASTDICE_MIN_VALUE;
        	this.maxValue = FASTDICE_MAX_VALUE;
        	break;

        case SLOWDICE:
        	this.setName("Slow Dice");
        	this.minValue = SLOWDICE_MIN_VALUE;
        	this.maxValue = SLOWDICE_MAX_VALUE;
        	break;

        default:
        	this.setName("Dice");
        	this.minValue = 1;
        	this.maxValue = 6;
        	break;
        }
    }

    public int roll() {
        return (int) ((java.lang.Math.random() * (maxValue - minValue + 1)) + minValue);
    }
}
