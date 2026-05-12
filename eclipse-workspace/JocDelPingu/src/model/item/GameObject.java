package model.item;


/**
 * Abstract base class for every item or pick-up that can live in a player's
 * inventory (snowballs, fish, dice variants...).
 *
 * <p>Subclasses such as {@code SnowBall}, {@code Fish} and {@code Dice}
 * inherit from this class and only add the behaviour specific to themselves
 * (e.g. {@code Dice.roll()}). The shared metadata - id, display name,
 * {@link ObjectType} and stack quantity - lives here so that the rest of
 * the game can treat all objects uniformly.</p>
 */
public abstract class GameObject {


    /////////////////////////////
    ///       FIELDS         ///
    /////////////////////////////

    // Pseudo-unique identifier generated at construction time. It is not
    // used to look items up in storage - it only helps when logging /
    // debugging to tell two instances of the same type apart.
    private int objectId;

    // Human-readable label shown in dialogs and tooltips ("Snowball", "Fish"...).
    private java.lang.String name;

    // Category of the object. The game logic switches on this to decide
    // what an item does when it is used.
    private ObjectType type;

    // Generic stack size. Most concrete subclasses prefer to track their
    // counts inside Inventory directly, so this field is rarely used.
    private int quantity;


    /////////////////////////////
    ///     CONSTRUCTOR      ///
    /////////////////////////////

    /**
     * Creates a new game object of the given {@link ObjectType} and assigns
     * it a random id so two instances can be distinguished in logs.
     *
     * @param type the category this object belongs to
     */
    public GameObject(ObjectType type) {
        // Random id is not a true UUID - collisions are acceptable because
        // the id is only used for human-readable logging, never for lookups.
        this.objectId = (int) (java.lang.Math.random() * 100000);
        this.type = type;
    }


    /////////////////////////////
    ///  GETTERS / SETTERS   ///
    /////////////////////////////

    /**
     * @return the random id assigned at construction time
     */
    public int getObjectId() {
        return objectId;
    }


    /**
     * @return the category of this object (snowball / fish / dice...)
     */
    public ObjectType getType() {
        return type;
    }


    /**
     * @return the display name (set by subclasses in their constructor)
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the display name. Subclasses call this from their constructor
     * so the rest of the game can show a localized / human-readable label.
     *
     * @param name the new display name
     */
    public void setName(java.lang.String name) {
    	this.name = name;
    }


    /**
     * @return the generic stack size for this object
     */
    public int getQuantity() {
    	return quantity;
    }


    /**
     * Updates the generic stack size for this object.
     *
     * @param quantity the new quantity
     */
    public void setQuantity(int quantity) {
    	this.quantity = quantity;
    }
}
