package ObjectManagers.objects.dices;

import ObjectManagers.ObjectType;
import ObjectManagers.objects.Dice;

public class FastDice extends Dice{

	public FastDice(ObjectType diceType) {
		super(diceType);
	}

	private ObjectType diceType = ObjectType.FASTDICE;
}
