package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_Event extends Square {

	public S_Event(SquareType type) {
		super(type);
		
	}
	
	@Override
	public SquareType getType() {
		return SquareType.EVENT;
	};
	
	@Override
	public String action(model.entity.Player player) {
		int randomEvent = (int)(Math.random() * 6);
		switch(randomEvent) {
			case 0:
				player.getInventory().addFish();
				return player.getName() + " found a Fish!";
			case 1:
				int snowballs = (int)(Math.random() * 3) + 1;
				player.getInventory().addSnowballs(snowballs);
				return player.getName() + " found " + snowballs + " Snowballs!";
			case 2:
				player.getInventory().addDice(model.item.ObjectType.FASTDICE);
				return player.getName() + " found a Fast Dice!";
			case 3:
				player.getInventory().addDice(model.item.ObjectType.SLOWDICE);
				return player.getName() + " found a Slow Dice!";
			case 4:
				player.setSkipNextTurn(true);
				return player.getName() + " got trapped in a blizzard! Loses a turn.";
			case 5:
				model.item.ObjectType lostItem = player.getInventory().removeRandomItem();
				if (lostItem != null) {
					return player.getName() + " dropped a " + lostItem + " due to strong winds!";
				}
				break;
		}
        return player.getName() + " triggered an event.";
	}

}
