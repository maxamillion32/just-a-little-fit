package group.g203.justalittlefit.database;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;

import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;
import group.g203.justalittlefit.model.Workout;

/**
 * Util class that creates a database config file to use for building the DAOs for the database.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    private static final Class<?>[] classes = new Class[] {
            Workout.class, Exercise.class, Set.class
    };

    // File path will need to be changed based on local config
    public static void main(String[] args) throws Exception {
        writeConfigFile(new File("C:\\Users\\s536564\\AndroidStudioProjects\\just-a-little-fit\\app\\src\\main\\res\\raw\\ormlite_config.txt"), classes);
    }
}
