package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit.AbstractDataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit.DataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit.DataProviderFragment;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit.MyDraggableSwipeableItemAdapter;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit.RecyclerListViewFragment;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AddExerciseDialog;
import group.g203.justalittlefit.dialog.AppBaseDialog;
import group.g203.justalittlefit.dialog.ConfirmDeleteExercisesDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.dialog.RenameDialog;
import group.g203.justalittlefit.listener.AddExerciseDialogListener;
import group.g203.justalittlefit.listener.ConfirmExercisesDeletionListener;
import group.g203.justalittlefit.listener.RenameDialogListener;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Creates an {@link group.g203.justalittlefit.model.Exercise} in the app.
 */
public class CreateEditExercise extends BaseNaviDrawerActivity implements ConfirmExercisesDeletionListener,
        AddExerciseDialogListener, RenameDialogListener {
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    FloatingActionButton fab;
    CoordinatorLayout clFab;
    Workout parentWorkout;
    boolean busRegistered;
    boolean reorderTriggeredByAddExercise;
    String addedExerciseName;
    @Bind(R.id.rlDefault)
    RelativeLayout rlDefault;
    @Bind(R.id.tvWorkoutHeader)
    TextView tvWorkoutHeader;
    @Bind(R.id.tvWorkoutName)
    TextView tvWorkoutName;
    Bundle savedBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            getIntent().getExtras().putBundle(Constants.SAVED_BUNDLE, savedInstanceState);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = getIntent().getExtras();
        if (Utils.isInBundleAndValid(extras, Constants.SAVED_BUNDLE)) {
            savedBundle = extras.getBundle(Constants.SAVED_BUNDLE);
        }
        super.onNewIntent(intent);
        setIntent(intent);
    }

    void displayExerciseList() {
        parentWorkout = getParentWorkout();
        if (parentWorkout != null) {
            DbFunctionObject getExercisesByWorkout = new DbFunctionObject(parentWorkout, DbConstants.GET_EXERCISES_BY_WORKOUT);
            new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(getExercisesByWorkout);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.exercise_list_error));
            hideProgressDialog();
        }
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            displayGeneralExerciseListError();
        } else if (event.getResult() instanceof Integer) {
            final Fragment recyclerFrag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
            MyDraggableSwipeableItemAdapter adapter =
                    ((RecyclerListViewFragment) recyclerFrag).getAdapter();
            DataProvider dataProvider =
                    (DataProvider) getDataProvider();
            if (Utils.dataProviderIsValid(dataProvider)) {
                if (adapter != null && dataProvider != null && dataProvider.getCount() >= 0) {
                    adapter.removeAllItems(dataProvider.getCount() - 1);
                    Utils.displayLongSimpleSnackbar(fab, getString(R.string.confirmDeleteExerciseDialog_success));
                    rlDefault.setVisibility(View.VISIBLE);
                } else {
                    Utils.displayLongSimpleSnackbar(fab, getString(R.string.deletion_exercise_error));
                }
            } else {
                Utils.exitActivityOnError(this);
            }
        } else if (event.getResult() instanceof java.util.Set) {
            // Data order saved
            if (reorderTriggeredByAddExercise) {
                // Call method to add exercise to view
                addExerciseToUI();
            } else {
                // onPause result returned and data order saved, reset boolean trigger
                reorderTriggeredByAddExercise = false;
            }
        } else if (event.getResult() instanceof List) {
            List<Exercise> exercises = (List<Exercise>) event.getResult();
            getSupportFragmentManager().beginTransaction()
                    .add(DataProviderFragment.newInstance(new ArrayList<>(exercises), Constants.EXERCISE),
                            FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, RecyclerListViewFragment.newInstance(true), FRAGMENT_LIST_VIEW)
                    .commitAllowingStateLoss();
            if (exercises.size() == 0) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else if (event.getResult() instanceof Boolean) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addExercise_success));
            displayExerciseList();
        } else if (event.getResult() instanceof Exercise) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.renameDialog_ExerciseSuccess));
            displayExerciseList();
        } else {
            displayGeneralExerciseListError();
        }
        hideProgressDialog();
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_createEdit);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
    }

    private void setupFloatingActionButton(final BaseNaviDrawerActivity activity) {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        clFab = (CoordinatorLayout) findViewById(R.id.clFab);
        clFab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_plus_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAddExerciseDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_delete_all:
                displayConfirmDeleteAllExercisesDialog();
                break;
            case R.id.action_info:
                displayInfoDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public AbstractDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        if (fragment != null) {
            return ((DataProviderFragment) fragment).getDataProvider();
        } else {
            return null;
        }
    }

    public void onItemRemoved(Object dataObject) {
        determineDefaultStatus();
        Exercise exercise = (Exercise) dataObject;

        Integer removeResult = Utils.removeExercise(exercise.getWorkout().getExercises(),
                Utils.ensureValidString(exercise.getName().trim()));
        if (removeResult == Constants.INT_ONE) {
            Utils.displayLongActionSnackbar(fab, getString(R.string.exercise_deleted),
                    Constants.UNDO, undoExerciseDelete(),
                    getResources().getColor(R.color.app_blue_gray));
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.exercise_modify_error));
            hideProgressDialog();
        }
    }

    public void onItemClicked(int position) {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            AbstractDataProvider.Data data = dataProvider.getItem(position);
            Exercise exercise = (Exercise) data.getDataObject();
            Intent createEditSet = new Intent(this, CreateEditSet.class);
            createEditSet.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.EXERCISE, exercise);
            createEditSet.putExtras(bundle);
            startActivity(createEditSet);
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    public void onItemLongClicked(int position) {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            AbstractDataProvider.Data data = dataProvider.getItem(position);
            Exercise exercise = (Exercise) data.getDataObject();
            FragmentManager fm = getSupportFragmentManager();
            RenameDialog dialog = RenameDialog.newInstance(exercise);
            dialog.show(fm, getString(R.string.renameDialog_Tag));
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    private View.OnClickListener undoExerciseDelete() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataProvider dataProvider =
                        (DataProvider) getDataProvider();
                if (Utils.dataProviderIsValid(dataProvider)) {
                    int position = dataProvider.undoLastRemoval();
                    final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
                    ((RecyclerListViewFragment) fragment).notifyItemInserted(position);
                    Utils.displayLongSimpleSnackbar(fab,
                            getString(R.string.exercise_removal_undone));
                    AbstractDataProvider.Data data = dataProvider.getItem(position);
                    Exercise exercise = (Exercise) data.getDataObject();
                    exercise.getWorkout().getExercises().add(exercise);
                    determineDefaultStatus();
                } else {
                    Utils.exitActivityOnError(getParent());
                }
            }
        };
    }

    private Workout getParentWorkout() {
        Bundle extras = getIntent().getExtras();
        if (Utils.isInBundleAndValid(extras, Constants.WORKOUT)) {
           return extras.getParcelable(Constants.WORKOUT);
        } else {
            return null;
        }
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.EXERCISES_NORM_CASE);
        dialog.show(fm, getString(R.string.infoDialogTagExercise));
    }

    private void displayConfirmDeleteAllExercisesDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmDeleteExercisesDialog dialog = new ConfirmDeleteExercisesDialog();
        dialog.show(fm, getString(R.string.confirmDeleteWorkoutsDialogTag));
    }

    @Override
    protected void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reorderExercises();
        unregisterBus();
        hideProgressDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressDialog();
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_exercise, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_exercise_title_string);
        if (savedBundle == null) {
            displayExerciseList();
        }
        formatAndSetWorkoutHeaderTexts();
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_createEdit);
        selectedItem.setChecked(true);
    }

    @Override
    public void onDeleteAllExercisesClick(AppBaseDialog dialog) {
        if (rlDefault.getVisibility() == View.VISIBLE) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.no_exercises_to_delete));
        } else {
            DataProvider dataProvider =
                    (DataProvider) getDataProvider();
            if (Utils.dataProviderIsValid(dataProvider)) {
                List<Exercise> exercises = (List<Exercise>) (Object) dataProvider.getDataObjects();
                DbFunctionObject deleteExercises =
                        new DbFunctionObject(exercises, DbConstants.DELETE_ALL_EXERCISES);
                new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(deleteExercises);
            } else {
                Utils.exitActivityOnError(this);
            }
        }
    }


    private void displayAddExerciseDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddExerciseDialog dialog = new AddExerciseDialog();
        dialog.show(fm, getString(R.string.addExerciseDialogTag));
    }

    private void registerBus() {
        if (!busRegistered) {
            BusFactory.getCreateEditExerciseBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getCreateEditExerciseBus().unregister(this);
            busRegistered = false;
        }
    }

    private void reorderExercises() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            List<Exercise> exercisesToSave = (List<Exercise>) (Object) dataProvider.getDataObjects();
            for (int i = 0; i < exercisesToSave.size(); i++) {
                exercisesToSave.get(i).setOrderNumber(i);
            }
            DbFunctionObject saveExercisesDfo =
                    new DbFunctionObject(exercisesToSave, DbConstants.UPDATE_EXERCISES);
            new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(saveExercisesDfo);
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    @Override
    public void onAddExerciseClick(AddExerciseDialog dialog) {
        showProgressDialog();
        reorderTriggeredByAddExercise = true;
        reorderExercises();
        addedExerciseName = Utils.ensureValidString(dialog.getAddExerciseText().getText().toString());
    }

    private void addExerciseToUI() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getDisplayNames() != null
                    && addedExerciseName != null) {
                if (!dataProvider.getDisplayNames().contains(addedExerciseName.trim())) {
                    Exercise newExercise = new Exercise(parentWorkout, addedExerciseName, dataProvider.getCount());
                    DbFunctionObject insertExercise = new DbFunctionObject(newExercise, DbConstants.INSERT_EXERCISE);
                    new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(insertExercise);
                } else {
                    Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error_already_exists));
                }
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_exercise_error));
            }
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    void determineDefaultStatus() {
        DataProvider dataProvider =
                (DataProvider)getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            if (dataProvider != null && dataProvider.getCount() == 0) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    void displayGeneralExerciseListError() {
        String errorMsg = getString(R.string.exercise_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }

    void formatAndSetWorkoutHeaderTexts() {
        Typeface face=Typeface.createFromAsset(getAssets(), Constants.CUSTOM_FONT_TTF);
        tvWorkoutHeader.setTypeface(face);
        tvWorkoutName.setText(Utils.ensureValidString(parentWorkout.getName()));
    }

    @Override
    public void onRenameClick(RenameDialog dialog) {
        Exercise exercise = dialog.getExercise();
        exercise.setName(Utils.ensureValidString(dialog.getRenameText().getText().toString()));
        DbFunctionObject updateExercise = new DbFunctionObject(exercise, DbConstants.UPDATE_EXERCISE);
        new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(updateExercise);
    }
}
