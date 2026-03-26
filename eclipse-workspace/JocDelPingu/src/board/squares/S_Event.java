package board.squares;

import board.Square;
import board.SquareType;

public class S_Event extends Square {

	public S_Event(SquareType type) {
		super(type);
		
	}
	
	@Override
	public SquareType getType() {
		return SquareType.EVENT;
	};
	
	@Override
	public void action(entity.Player player) {
		int randomEvent = (int)(Math.random() * 6);
		switch(randomEvent) {
			case 0:
				player.getInventory().addFish();
				System.out.println(player.getName() + " found a Fish!");
				break;
			case 1:
				int snowballs = (int)(Math.random() * 3) + 1;
				player.getInventory().addSnowballs(snowballs);
				System.out.println(player.getName() + " found " + snowballs + " Snowballs!");
				break;
			case 2:
				player.getInventory().addDice(ObjectManagers.ObjectType.FASTDICE);
				System.out.println(player.getName() + " found a Fast Dice!");
				break;
			case 3:
				player.getInventory().addDice(ObjectManagers.ObjectType.SLOWDICE);
				System.out.println(player.getName() + " found a Slow Dice!");
				break;
			case 4:
				player.setSkipNextTurn(true);
				System.out.println(player.getName() + " got trapped in a blizzard! Loses a turn.");
				break;
			case 5:
				ObjectManagers.ObjectType lostItem = player.getInventory().removeRandomItem();
				if (lostItem != null) {
					System.out.println(player.getName() + " dropped a " + lostItem + " due to strong winds!");
				}
				break;
		}
	}

}
