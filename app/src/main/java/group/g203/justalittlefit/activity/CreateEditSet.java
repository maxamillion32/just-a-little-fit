package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.LinkedHashSet;
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
import group.g203.justalittlefit.dialog.AddSetDialog;
import group.g203.justalittlefit.dialog.AppBaseDialog;
import group.g203.justalittlefit.dialog.ConfirmDeleteSetsDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.listener.AddSetDialogListener;
import group.g203.justalittlefit.listener.ConfirmSetsDeletionListener;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Creates a {@link group.g203.justalittlefit.model.Set} in the app.
 */
public class CreateEditSet extends BaseActivity implements ConfirmSetsDeletionListener, AddSetDialogListener {
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    FloatingActionButton fab;
    CoordinatorLayout clFab;
    Exercise parentExercise;
    boolean busRegistered;
    boolean reorderTriggeredByAddSet;
    boolean reorderTriggeredByEditSet;
    Set addedSet;
    @Bind(R.id.rlDefault)
    RelativeLayout rlDefault;
    @Bind({R.id.tvExerciseHeader, R.id.tvWorkoutHeader})
    List<TextView> headerTextViews;
    @Bind(R.id.tvWorkoutName)
    TextView tvWorkoutName;
    @Bind(R.id.tvExerciseName)
    TextView tvExerciseName;
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

    void displaySetList() {
        showProgressDialog();
        parentExercise = getParentExercise();
        if (parentExercise != null) {
            DbFunctionObject getSetsByExercise = new DbFunctionObject(parentExercise, DbConstants.GET_SETS_BY_EXERCISE);
            new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(getSetsByExercise);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.exercise_list_error));
            hideProgressDialog();
        }

        if (reorderTriggeredByEditSet) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.editSet_success));
            reorderTriggeredByEditSet = false;
            hideProgressDialog();
        }
    }

    private void setupFloatingActionButton(final BaseActivity activity) {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        clFab = (CoordinatorLayout) findViewById(R.id.clBase);
        clFab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_plus_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAddSetDialog();
            }
        });
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            displayGeneralSetListError();
        } else if (event.getResult() instanceof List) {
            List<Set> sets = (List<Set>) event.getResult();
            LinkedHashSet<Set> setsObj = new LinkedHashSet<>(sets);
            getSupportFragmentManager().beginTransaction()
                    .add(DataProviderFragment.newInstance(setsObj, Constants.SET),
                            FRAGMENT_TAG_DATA_PROVIDER)
                    .commitAllowingStateLoss();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, RecyclerListViewFragment.newInstance(true), FRAGMENT_LIST_VIEW)
                    .commitAllowingStateLoss();
            if (sets.size() == 0) {
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                rlDefault.setVisibility(View.INVISIBLE);
            }
        } else if (event.getResult() instanceof java.util.Set) {
            // Data order saved
            if (reorderTriggeredByAddSet) {
                addSetToUI();
            } else {
                // onPause result returned and data order saved, reset boolean trigger
                reorderTriggeredByAddSet = false;
            }
        } else if (event.getResult() instanceof Boolean) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.addSet_success));
            DbFunctionObject getSetsByExercise = new DbFunctionObject(parentExercise, DbConstants.GET_SETS_BY_EXERCISE);
            new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(getSetsByExercise);
        } else if (event.getResult() instanceof Double) {
            displaySetList();
        } else if (event.getResult() instanceof String) {
            // onPause delete returned, reorder sets before leaving activity
            reorderSets();
        } else if (event.getResult() instanceof Integer) {
            final Fragment recyclerFrag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
            MyDraggableSwipeableItemAdapter adapter =
                    ((RecyclerListViewFragment) recyclerFrag).getAdapter();
            DataProvider dataProvider =
                    (DataProvider) getDataProvider();
            if (Utils.dataProviderIsValid(dataProvider)) {
                if (adapter != null && dataProvider != null && dataProvider.getCount() >= 0) {
                    adapter.removeAllItems(dataProvider.getCount() - 1);
                    Utils.displayLongSimpleSnackbar(fab, getString(R.string.confirmDeleteSetDialog_success));
                    rlDefault.setVisibility(View.VISIBLE);
                } else {
                    Utils.displayLongSimpleSnackbar(fab, getString(R.string.deletion_set_error));
                }
            } else {
                Utils.exitActivityOnError(this);
            }
        } else {
            displayGeneralSetListError();
        }
        hideProgressDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                displayConfirmDeleteAllSetsDialog();
                break;
            case R.id.action_info:
                displayInfoDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemRemoved(Object dataObject) {
        determineDefaultStatus();
        Set set = (Set) dataObject;

        Integer removeResult = Utils.removeSet(set.getExercise().getSets(),
                Utils.ensureValidString(set.toString().trim()));
        if (removeResult == Constants.INT_ONE) {
            Utils.displayLongActionSnackbar(fab, getString(R.string.set_deleted),
                    Constants.UNDO, undoSetDelete(),
                    getResources().getColor(R.color.app_blue_gray));
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.set_modify_error));
            hideProgressDialog();
        }
    }

    public void onItemClicked(int position) {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            AbstractDataProvider.Data data = dataProvider.getItem(position);
            Set set = (Set) data.getDataObject();
            displayAddSetDialogUponEdit(set);
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    private View.OnClickListener undoSetDelete() {
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
                    Set set = (Set) data.getDataObject();
                    set.getExercise().getSets().add(set);
                    determineDefaultStatus();
                } else {
                    Utils.exitActivityOnError(getParent());
                }
            }
        };
    }

    private Exercise getParentExercise() {
        Bundle extras = getIntent().getExtras();
        if (Utils.isInBundleAndValid(extras, Constants.EXERCISE)) {
            return extras.getParcelable(Constants.EXERCISE);
        } else {
            return null;
        }
    }

    public AbstractDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        if (fragment != null) {
            return ((DataProviderFragment) fragment).getDataProvider();
        } else {
            return null;
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            BusFactory.getCreateEditSetBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getCreateEditSetBus().unregister(this);
            busRegistered = false;
        }
    }

    private void reorderSets() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            List<Set> setsToReorder = (List<Set>) (Object) dataProvider.getDataObjects();
            for (int i = 0; i < setsToReorder.size(); i++) {
                setsToReorder.get(i).setOrderNumber(i);
            }
            DbFunctionObject reorderSetsDfo =
                    new DbFunctionObject(setsToReorder, DbConstants.UPDATE_SETS);
            new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(reorderSetsDfo);
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    private void addSetToUI() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        if (Utils.dataProviderIsValid(dataProvider)) {
            if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getDisplayNames() != null
                    && addedSet != null) {
                addedSet.setExercise(parentExercise);
                addedSet.setOrderNumber(dataProvider.getCount());
                DbFunctionObject insertWorkoutSet = new DbFunctionObject(addedSet, DbConstants.INSERT_SET);
                new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(insertWorkoutSet);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_set_error));
                hideProgressDialog();
            }
        } else {
            Utils.exitActivityOnError(this);
        }
    }

    void determineDefaultStatus() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
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

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.SETS_NORM_CASE);
        dialog.show(fm, getString(R.string.infoDialogTagSet));
    }

    private void displayConfirmDeleteAllSetsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmDeleteSetsDialog dialog = new ConfirmDeleteSetsDialog();
        dialog.show(fm, getString(R.string.confirmDeleteWorkoutsDialogTag));
    }

    private void displayAddSetDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AddSetDialog dialog = new AddSetDialog();
        dialog.show(fm, getString(R.string.addSetDialogTag));
    }

    private void displayAddSetDialogUponEdit(Set set) {
        FragmentManager fm = getSupportFragmentManager();
        AddSetDialog dialog = AddSetDialog.newInstance(set);
        dialog.show(fm, getString(R.string.addSetDialogTag));
    }

    void formatAndSetWorkoutHeaderTexts() {
        for (TextView tv : headerTextViews) {
            Typeface face=Typeface.createFromAsset(getAssets(), Constants.CUSTOM_FONT_TTF);
            tv.setTypeface(face);
        }
        if (parentExercise != null && parentExercise.getWorkout() != null) {
            tvWorkoutName.setText(Utils.ensureValidString(parentExercise.getWorkout().getName()));
            tvExerciseName.setText(Utils.ensureValidString(parentExercise.getName()));
        }
    }

    @Override
    protected void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressDialog();
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_set, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        if (savedBundle == null) {
            displaySetList();
        }
        formatAndSetWorkoutHeaderTexts();
        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_set_title_string);
        handleNaviSelectionColor(Constants.CREATE_EDIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBus();
        reorderSets();
        hideProgressDialog();
    }

    @Override
    public void onDeleteAllSetsClick(AppBaseDialog dialog) {
        if (rlDefault.getVisibility() == View.VISIBLE) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.no_sets_to_delete));
        } else {
            DataProvider dataProvider =
                    (DataProvider) getDataProvider();
            if (Utils.dataProviderIsValid(dataProvider)) {
                List<Set> sets = (List<Set>) (Object) dataProvider.getDataObjects();
                DbFunctionObject deleteSets =
                        new DbFunctionObject(sets, DbConstants.DELETE_ALL_SETS);
                new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(deleteSets);
            } else {
                Utils.exitActivityOnError(this);
            }
        }
    }

    void displayGeneralSetListError() {
        String errorMsg = getString(R.string.set_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }

    @Override
    public void onAddSetClick(AddSetDialog dialog) {
        reorderTriggeredByAddSet = true;
        reorderSets();
        if (dialog.getRbWeightedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            String exerciseCd = Constants.WEIGHTS;
            int weight = Utils.returnValidNumberFromEditText(dialog.getEtWeightAmount());
            String weightCd;
            if (dialog.getRbLbs().isChecked()) {
                weightCd = Constants.LBS;
            } else {
                weightCd = Constants.KGS;
            }
            addedSet = new Set(reps, weightCd, exerciseCd, weight);
        } else if (dialog.getRbTimedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
            Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
            Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
            String exerciseCd = Constants.LOGGED_TIMED;
            addedSet = new Set(reps, exerciseCd, hours, mins, seconds);
        } else if (dialog.getRbNonWeightedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            String exerciseCd = Constants.NA;
            addedSet = new Set(reps, exerciseCd);
        }
    }

    @Override
    public void onEditSetClick(AddSetDialog dialog) {
        showProgressDialog();
        reorderTriggeredByEditSet = true;
        Set set = dialog.getSet();

        if (dialog.getRbWeightedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            String exerciseCd = Constants.WEIGHTS;
            int weight = Utils.returnValidNumberFromEditText(dialog.getEtWeightAmount());
            String weightCd;
            if (dialog.getRbLbs().isChecked()) {
                weightCd = Constants.LBS;
            } else {
                weightCd = Constants.KGS;
            }
            set.setReps(reps);
            set.setWeightTypeCode(weightCd);
            set.setExerciseTypeCode(exerciseCd);
            set.setWeight(weight);
        } else if (dialog.getRbTimedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
            Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
            Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
            String exerciseCd = Constants.LOGGED_TIMED;
            set.setReps(reps);
            set.setExerciseTypeCode(exerciseCd);
            set.setMinutes(mins);
            set.setHours(hours);
            set.setSeconds(seconds);
        } else if (dialog.getRbNonWeightedSet().isChecked()) {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtRepCount());
            String exerciseCd = Constants.NA;
            set.setReps(reps);
            set.setExerciseTypeCode(exerciseCd);
        }

        String errMsg = Utils.returnEditSetErrorString(set);

        if (Utils.isEmptyString(errMsg)) {
            dialog.dismiss();
            DbFunctionObject editSetDfo =
                    new DbFunctionObject(set, DbConstants.UPDATE_SET);
            new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(editSetDfo);
        } else {
            Utils.displayLongToast(this, errMsg);
        }
        hideProgressDialog();
    }
}
