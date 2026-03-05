package ObjectManagers.objects.dices;

import ObjectManagers.ObjectType;
import ObjectManagers.objects.Dice;

public class SlowDice extends Dice {

	public SlowDice(ObjectType diceType) {
		super(diceType);
	}

	private ObjectType diceType = ObjectType.FASTDICE;
}
