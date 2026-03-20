package ObjectManagers;

public abstract class Object extends java.lang.Object {
    
    protected int objectId;
    protected java.lang.String name;
    protected ObjectType type;
    
    public Object(ObjectType type) {
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
}