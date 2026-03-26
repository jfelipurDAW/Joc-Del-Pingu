package board.squares;

import board.Square;
import board.SquareType;

public class S_Bear extends Square {

	public S_Bear(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.BEAR;
	};
	
	
	@Override
	public void action(entity.Player player) {
		if (player.getInventory().getFishQuantity() > 0) {
			player.getInventory().useObject(ObjectManagers.ObjectType.FISH, 1);
			System.out.println(player.getName() + " feed the Bear a fish and avoided returning to start!");
		} else {
			player.setSquare(0);
			System.out.println(player.getName() + " attacked by Bear! Back to start!");
		}
	};

}
