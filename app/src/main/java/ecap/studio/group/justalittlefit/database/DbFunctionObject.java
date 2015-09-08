package ecap.studio.group.justalittlefit.database;

/**
 * Object that encapsulates either a single
 * {@link ecap.studio.group.justalittlefit.model.Workout},
 * {@link ecap.studio.group.justalittlefit.model.Exercise},
 * {@link ecap.studio.group.justalittlefit.model.Set} or
 * list of these objects and an int that represents a CRUD database action.
 */
public class DbFunctionObject {

    private Object dbObject;
    private int functionInt;

    public DbFunctionObject() {
    }

    public DbFunctionObject(Object dbObject, int functionInt) {
        this.dbObject = dbObject;
        this.functionInt = functionInt;
    }

    public Object getDbObject() {
        return dbObject;
    }

    public void setDbObject(Object dbObject) {
        this.dbObject = dbObject;
    }

    public int getFunctionInt() {
        return functionInt;
    }

    public void setFunctionInt(int functionInt) {
        this.functionInt = functionInt;
    }

}
