package ecap.studio.group.justalittlefit.database;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;

import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.SuperSet;
import ecap.studio.group.justalittlefit.model.Workout;

/**
 * Util class that creates a database config file to use for building the DAOs for the database.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    private static final Class<?>[] classes = new Class[] {
            Workout.class, Exercise.class, Set.class, SuperSet.class
    };

    public static void main(String[] args) throws Exception {
        writeConfigFile(new File("C:/Users/Triest/AndroidStudioProjects/JustALittleFit/app/src/main/res/raw/ormlite_config.txt"), classes);
    }
}
