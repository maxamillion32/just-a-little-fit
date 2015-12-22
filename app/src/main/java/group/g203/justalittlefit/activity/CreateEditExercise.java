package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
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

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.AbstractDataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.DataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.DataProviderFragment;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.MyDraggableSwipeableItemAdapter;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.RecyclerListViewFragment;
import group.g203.justalittlefit.bus.CreateEditExerciseBus;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AddExerciseDialog;
import group.g203.justalittlefit.dialog.AppBaseDialog;
import group.g203.justalittlefit.dialog.ConfirmDeleteExercisesDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.listener.AddExerciseDialogListener;
import group.g203.justalittlefit.listener.ConfirmExercisesDeletionListener;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

public class CreateEditExercise extends BaseNaviDrawerActivity implements ConfirmExercisesDeletionListener,
        AddExerciseDialogListener {
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    FloatingActionButton fab;
    CoordinatorLayout clFab;
    Workout parentWorkout;
    boolean busRegistered;
    boolean reorderTriggeredByAddExercise;
    String addedExerciseName;
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_exercise, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_exercise_title_string);

        if (savedInstanceState == null) {
            displayExerciseList();
        }
    }

    void displayExerciseList() {
        showProgressBar();
        parentWorkout = getParentWorkout();
        if (parentWorkout != null) {
            DbFunctionObject getExercisesByWorkout = new DbFunctionObject(parentWorkout, DbConstants.GET_EXERCISES_BY_WORKOUT);
            new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(getExercisesByWorkout);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.exercise_list_error));
        }
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        hideProgressBar();
        if (event == null || event.getResult() == null) {
            displayGeneralExerciseListError();
        } else if (event.getResult() instanceof Integer) {
            final Fragment recyclerFrag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
            MyDraggableSwipeableItemAdapter adapter =
                    ((RecyclerListViewFragment) recyclerFrag).getAdapter();
            DataProvider dataProvider =
                    (DataProvider) getDataProvider();
            if (adapter != null && dataProvider != null && dataProvider.getCount() >= 0) {
                adapter.removeAllItems(dataProvider.getCount() - 1);
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.confirmDeleteExerciseDialog_success));
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.deletion_exercise_error));
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
                    .replace(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commitAllowingStateLoss();
            if (exercises.size() == 0) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else if (event.getResult() instanceof Boolean) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addExercise_success));
            displayExerciseList();
        } else if (event.getResult() instanceof String) {
            // onPause delete returned, reorder exercises before leaving activity
            reorderExercises();
        } else {
            displayGeneralExerciseListError();
        }
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
        return ((DataProviderFragment) fragment).getDataProvider();
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
        }
    }

    public void onItemClicked(int position) {
        AbstractDataProvider.Data data = getDataProvider().getItem(position);
        Exercise exercise = (Exercise) data.getDataObject();
        Intent createEditSet = new Intent(this, CreateEditSet.class);
        createEditSet.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.EXERCISE, exercise);
        createEditSet.putExtras(bundle);
        startActivity(createEditSet);
    }

    private View.OnClickListener undoExerciseDelete() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getDataProvider().undoLastRemoval();
                final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
                ((RecyclerListViewFragment) fragment).notifyItemInserted(position);
                Utils.displayLongSimpleSnackbar(fab,
                        getString(R.string.exercise_removal_undone));
                AbstractDataProvider.Data data = getDataProvider().getItem(position);
                Exercise exercise = (Exercise) data.getDataObject();
                exercise.getWorkout().getExercises().add(exercise);
                determineDefaultStatus();
            }
        };
    }

    private Workout getParentWorkout() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.WORKOUT)) {
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
        unregisterBus();
        reorderExercises();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!busRegistered) {
            registerBus();
        }
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_createEdit);
        selectedItem.setChecked(true);
    }

    @Override
    public void onDeleteAllExercisesClick(AppBaseDialog dialog) {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        List<Exercise> exercises = (List<Exercise>) (Object) dataProvider.getDataObjects();
        DbFunctionObject deleteExercises =
                new DbFunctionObject(exercises, DbConstants.DELETE_ALL_EXERCISES);
        new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(deleteExercises);
    }


    private void displayAddExerciseDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddExerciseDialog dialog = new AddExerciseDialog();
        dialog.show(fm, getString(R.string.addExerciseDialogTag));
    }

    public RecyclerListViewFragment getRecyclerViewFrag() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        return ((RecyclerListViewFragment) fragment);
    }

    private void registerBus() {
        if (!busRegistered) {
            CreateEditExerciseBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            CreateEditExerciseBus.getInstance().unregister(this);
            busRegistered = false;
        }
    }

    void showProgressBar() {
        if (isProgressBarReady()) {
            getRecyclerViewFrag().getProgressBar().setVisibility(View.VISIBLE);
        }
    }

    void hideProgressBar() {
        if (isProgressBarReady()) {
            getRecyclerViewFrag().getProgressBar().setVisibility(View.INVISIBLE);
        }
    }

    private void reorderExercises() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        List<Exercise> exercisesToSave = (List<Exercise>) (Object) dataProvider.getDataObjects();
        for (int i = 0; i < exercisesToSave.size(); i++) {
            exercisesToSave.get(i).setOrderNumber(i);
        }
        DbFunctionObject saveExercisesDfo =
                new DbFunctionObject(exercisesToSave, DbConstants.UPDATE_EXERCISES);
        new DbAsyncTask(Constants.CREATE_EDIT_EXERCISE).execute(saveExercisesDfo);
    }

    @Override
    public void onAddExerciseClick(AddExerciseDialog dialog) {
        showProgressBar();
        reorderTriggeredByAddExercise = true;
        reorderExercises();
        addedExerciseName = Utils.ensureValidString(dialog.getAddExerciseText().getText().toString());
    }

    private void addExerciseToUI() {
        DataProvider dataProvider =
                (DataProvider)getDataProvider();
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
    }

    void determineDefaultStatus() {
        DataProvider dataProvider =
                (DataProvider)getDataProvider();
        if (dataProvider != null && dataProvider.getCount() == 0) {
            rlDefault.setVisibility(View.VISIBLE);
        } else {
            rlDefault.setVisibility(View.INVISIBLE);
        }
    }

    void displayGeneralExerciseListError() {
        String errorMsg = getString(R.string.exercise_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }
}
