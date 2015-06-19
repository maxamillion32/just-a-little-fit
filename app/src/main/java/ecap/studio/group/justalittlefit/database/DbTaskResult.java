package ecap.studio.group.justalittlefit.database;

/**
 * Encapsulates the result of a database operation.
 */
public class DbTaskResult {

    private Object result;

    public DbTaskResult() {
    }

    public DbTaskResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
