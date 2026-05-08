package model.entity;

import model.board.Board;
import model.board.EventManager;
import model.board.SquareType;
import model.item.ObjectType;

public abstract class Entity {
	
	private int entityId;
	private int numSquare;
	private EntityType type;
	private Board board;
	private boolean skipNextTurn;

	// Transient visual state — not persisted in saves
	private boolean facingRight = true;
	private boolean damaged = false;

	// Last event result for UI display
	private EventManager.EventResult lastEvent;

	public int getSquareIndex() {
		return this.numSquare;
	}
	
	/**
	 * Advance the entity by a number of squares, clamped to not exceed the board.
	 * Returns true if the entity reached or passed the END square.
	 * Updates facingRight based on direction of movement.
	 */
	public boolean advance(int squares) {
		if (squares > 0)      this.facingRight = true;
		else if (squares < 0) this.facingRight = false;

		int newPos = this.numSquare + squares;
		if (newPos >= Board.MAX_SQUARES - 1) {
			this.numSquare = Board.MAX_SQUARES - 1;
			return true; // reached the end!
		} else {
			this.numSquare = newPos;
			return false;
		}
	}
	
	public EntityType getType() {
		return this.type;
	}
	
	public String getName() {
		return null;
	}
	
	public void setBoard(Board board) {
		this.board = board;
	}
	
	public void setSquare(int newPosition) {
		int clamped = Math.max(0, Math.min(newPosition, Board.MAX_SQUARES - 1));
		if (clamped > this.numSquare)      this.facingRight = true;
		else if (clamped < this.numSquare) this.facingRight = false;
		this.numSquare = clamped;
	}
	
	/**
	 * Process the effect of landing on the current square.
	 * Returns a description of what happened for the game log.
	 */
    // Handled by BoardManager now
	
	// ---------- Getters / Setters ----------
	
	public EventManager.EventResult getLastEvent() {
		return lastEvent;
	}
	
	public void clearLastEvent() {
		lastEvent = null;
	}
	
	public void setLastEvent(EventManager.EventResult event) {
		this.lastEvent = event;
	}
	
	public boolean shouldSkipNextTurn() {
		return skipNextTurn;
	}
	
	public void setSkipNextTurn(boolean skip) {
		this.skipNextTurn = skip;
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}
	
	public int getNumSquare() {
		return numSquare;
	}

	public void setNumSquare(int numSquare) {
		if (numSquare > this.numSquare)      this.facingRight = true;
		else if (numSquare < this.numSquare) this.facingRight = false;
		this.numSquare = numSquare;
	}

	public boolean isFacingRight() {
		return facingRight;
	}

	public void setFacingRight(boolean facingRight) {
		this.facingRight = facingRight;
	}

	public boolean isDamaged() {
		return damaged;
	}

	public void setDamaged(boolean damaged) {
		this.damaged = damaged;
	}
	
	public Board getBoard() {
		return board;
	}
	
	public void setType(EntityType type) {
		this.type = type;
	}
}
