package model.item;

public abstract class GameObject {
    
    private int objectId;
    private java.lang.String name;
    private ObjectType type;
    private int quantity;
    
    public GameObject(ObjectType type) {
        this.objectId = (int) (java.lang.Math.random() * 100000);
        this.type = type;
    }
    
    public int getObjectId() {
        return objectId;
    }
    
    public ObjectType getType() {
        return type;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
    	this.name = name;
    }
    
    public int getQuantity() {
    	return quantity;
    }
    
    public void setQuantity(int quantity) {
    	this.quantity = quantity;
    }
}