package model.entity;


/**
 * Discriminator for the two kinds of entities that exist on the board.
 *
 * <p>Using an enum instead of {@code instanceof} checks keeps the
 * branching logic (sprite selection, turn order, save serialisation, ...)
 * explicit and refactor-safe.</p>
 *
 * <ul>
 *   <li>{@link #PLAYER} — human-controlled penguin.</li>
 *   <li>{@link #SEAL}   — CPU-controlled predator that chases players.</li>
 * </ul>
 */
public enum EntityType {

    PLAYER,
    SEAL

}
