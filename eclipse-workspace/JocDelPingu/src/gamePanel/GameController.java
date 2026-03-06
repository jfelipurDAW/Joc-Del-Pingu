package gamePanel;

import board.TurnController;
import entity.Player;

public class GameController {

	private TurnController turn = new TurnController();
	
	public void startGame() {
		System.out.println("-----------GAME STARTED-----------");
		addplayers();
		tryTurns();
	}

    private void tryTurns() {
        for (int i = 0; i < 10; i++) {
      	  System.out.println(turn.getCurrentTurn().getName());
      	  turn.nextTurn();        	
        }
  	}

  	private void addplayers() {
		turn.addPlayer(new Player("Walker", "ffffff"));
      	turn.addPlayer(new Player("Marta", "ffffff"));
      	turn.addPlayer(new Player("Badre", "ffffff"));
  	}
}
