package ecap.studio.group.justalittlefit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;

/**
 * Database helper class used to manage the creation and upgrading of the database.
 * Also handles DAO (Database Access Object) logic.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "justALittleFit.db";
    /** Version number of the database...increment this on any update to database schema */
    private static final int DATABASE_VERSION = 1;
    /** The DAO object for {@link Workout}*/
    private Dao<Workout, Integer> workoutDao = null;
    /** The DAO object for {@link Exercise}*/
    private Dao<Exercise, Integer> exerciseDao = null;
    /** The DAO object for {@link Set}*/
    private Dao<Set, Integer> setDao = null;
    /** A helper class instance for DAO access*/
    private DaoHelper daoHelper;

    public DatabaseHelper(Context context) throws SQLException {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        daoHelper = DaoHelper.getInstance();
        daoHelper.setExerciseDao(getExerciseDao());
        daoHelper.setSetDao(getSetDao());
        daoHelper.setWorkoutDao(getWorkoutDao());
    }

    /**
     * Called when the database is first created.
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate executing");
            //Create database tables for Workout, Exercise, Set
            TableUtils.createTable(connectionSource, Workout.class);
            TableUtils.createTable(connectionSource, Exercise.class);
            TableUtils.createTable(connectionSource, Set.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Database Access Object (DAO) for {@link Workout}.
     * It will create it or return the cached DAO.
     * @throws SQLException
     */
    public Dao<Workout, Integer> getWorkoutDao() throws SQLException {
        if (workoutDao == null) {
            workoutDao = getDao(Workout.class);
        }
        return workoutDao;
    }

    /**
     * Returns the Database Access Object (DAO) for {@link Exercise}.
     * It will create it or return the cached DAO.
     * @throws SQLException
     */
    public Dao<Exercise, Integer> getExerciseDao() throws SQLException {
        if (exerciseDao == null) {
            exerciseDao = getDao(Exercise.class);
        }
        return exerciseDao;
    }

    /**
     * Returns the Database Access Object (DAO) for {@link Set}.
     * It will create it or return the cached DAO.
     * @throws SQLException
     */
    public Dao<Set, Integer> getSetDao() throws SQLException {
        if (setDao == null) {
            setDao = getDao(Set.class);
        }
        return setDao;
    }

    /**
     * Called when database is upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        workoutDao = null;
        exerciseDao = null;
        setDao = null;
    }
}
