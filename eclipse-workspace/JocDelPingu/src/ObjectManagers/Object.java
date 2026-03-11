package ObjectManagers;

public abstract class Object {
    
    protected int objectId;
    protected String name;
    protected ObjectType type;
    
    public Object(ObjectType type) {
        this.objectId = (int) (Math.random() * 100000);
        this.type = type;
        this.name = name;
    }
    
    
    public int getObjectId() {
        return objectId;
    }
    
    public ObjectType getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return name;
    }
}