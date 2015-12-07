package group.g203.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.AbstractDataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.DataProvider;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.DataProviderFragment;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.MyDraggableSwipeableItemAdapter;
import group.g203.justalittlefit.advanced_recyclerview.rv_create_edit_view.RecyclerListViewFragment;
import group.g203.justalittlefit.bus.CreateEditSetBus;
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
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

public class CreateEditSet extends BaseNaviDrawerActivity implements ConfirmSetsDeletionListener, AddSetDialogListener {
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
    @InjectView(R.id.rlDefault)
    RelativeLayout rlDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBus();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_create_edit_set, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);

        if (savedInstanceState == null) {
           displaySetList();
        }

        setupFloatingActionButton(this);
        setTitle(R.string.create_edit_set_title_string);
    }

    void displaySetList() {
        showProgressDialog();
        parentExercise = getParentExercise();
        if (parentExercise != null) {
            DbFunctionObject getSetsByExercise = new DbFunctionObject(parentExercise, DbConstants.GET_SETS_BY_EXERCISE);
            new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(getSetsByExercise);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.exercise_list_error));
        }

        if (reorderTriggeredByEditSet) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.editSet_success));
            reorderTriggeredByEditSet = false;
        }
    }

    private void setupFloatingActionButton(final BaseNaviDrawerActivity activity) {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        clFab = (CoordinatorLayout) findViewById(R.id.clFab);
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
        hideProgressDialog();
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
                    .replace(R.id.container, new RecyclerListViewFragment(), FRAGMENT_LIST_VIEW)
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
            if (adapter != null && dataProvider != null && dataProvider.getCount() >= 0) {
                adapter.removeAllItems(dataProvider.getCount() - 1);
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.confirmDeleteSetDialog_success));
                rlDefault.setVisibility(View.VISIBLE);
            } else {
                Utils.displayLongSimpleSnackbar(fab, getString(R.string.deletion_set_error));
            }
        } else {
            displayGeneralSetListError();
        }
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
            Utils.displayLongActionSnackbar(fab, getString(R.string.set_modify_error),
                    Constants.UNDO, undoSetDelete(),
                    getResources().getColor(R.color.app_blue_gray));
        }
    }

    public void onItemClicked(int position) {
        AbstractDataProvider.Data data = getDataProvider().getItem(position);
        Set set = (Set) data.getDataObject();
        displayAddSetDialogUponEdit(set);
    }

    private View.OnClickListener undoSetDelete() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getDataProvider().undoLastRemoval();
                final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
                ((RecyclerListViewFragment) fragment).notifyItemInserted(position);
                Utils.displayLongSimpleSnackbar(fab,
                        getString(R.string.exercise_removal_undone));
                AbstractDataProvider.Data data = getDataProvider().getItem(position);
                Set set = (Set) data.getDataObject();
                set.getExercise().getSets().add(set);
                determineDefaultStatus();
            }
        };
    }

    private Exercise getParentExercise() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.EXERCISE)) {
            return extras.getParcelable(Constants.EXERCISE);
        } else {
            return null;
        }
    }

    public AbstractDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        return ((DataProviderFragment) fragment).getDataProvider();
    }

    private void registerBus() {
        if (!busRegistered) {
            CreateEditSetBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            CreateEditSetBus.getInstance().unregister(this);
            busRegistered = false;
        }
    }

    public RecyclerListViewFragment getRecyclerViewFrag() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        return ((RecyclerListViewFragment) fragment);
    }

    private void reorderSets() {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        List<Set> setsToReorder = (List<Set>) (Object) dataProvider.getDataObjects();
        for (int i = 0; i < setsToReorder.size(); i++) {
            setsToReorder.get(i).setOrderNumber(i);
        }
        DbFunctionObject reorderSetsDfo =
                new DbFunctionObject(setsToReorder, DbConstants.UPDATE_SETS);
        new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(reorderSetsDfo);
    }

    private void addSetToUI() {
        DataProvider dataProvider =
                (DataProvider)getDataProvider();
        if (dataProvider != null && dataProvider.getCount() >= 0 && dataProvider.getDisplayNames() != null
                && addedSet != null) {
                addedSet.setExercise(parentExercise);
                addedSet.setOrderNumber(dataProvider.getCount());
                DbFunctionObject insertWorkoutSet = new DbFunctionObject(addedSet, DbConstants.INSERT_SET);
                new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(insertWorkoutSet);
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.add_set_error));
        }
    }

    void showProgressDialog() {
        if (isProgressDialogReady()) {
            getRecyclerViewFrag().getProgressDialog().setVisibility(View.VISIBLE);
        }
    }

    void hideProgressDialog() {
        if (isProgressDialogReady()) {
            getRecyclerViewFrag().getProgressDialog().setVisibility(View.INVISIBLE);
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

    @Override
    protected void onDestroy() {
        unregisterBus();
        super.onDestroy();
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
    protected void onPause() {
        super.onPause();
        unregisterBus();
        reorderSets();
    }

    @Override
    public void onDeleteAllSetsClick(AppBaseDialog dialog) {
        DataProvider dataProvider =
                (DataProvider) getDataProvider();
        List<Set> sets = (List<Set>) (Object) dataProvider.getDataObjects();
        DbFunctionObject deleteSets =
                new DbFunctionObject(sets, DbConstants.DELETE_ALL_SETS);
        new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(deleteSets);
    }

    void displayGeneralSetListError() {
        String errorMsg = getString(R.string.set_list_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongSimpleSnackbar(this.findViewById(R.id.fab), errorMsg);
    }

    @Override
    public void onAddSetClick(AddSetDialog dialog) {
        showProgressDialog();
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
        } else {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtTimedRepCount());
            Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
            Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
            Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
            String exerciseCd = Constants.LOGGED_TIMED;
            addedSet = new Set(reps, exerciseCd, hours, mins, seconds);
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
        } else {
            int reps = Utils.returnValidNumberFromEditText(dialog.getEtTimedRepCount());
            Integer hours = Utils.returnValidNumberFromEditText(dialog.getEtHours());
            Integer mins = Utils.returnValidNumberFromEditText(dialog.getEtMins());
            Integer seconds = Utils.returnValidNumberFromEditText(dialog.getEtSeconds());
            String exerciseCd = Constants.LOGGED_TIMED;
            set.setReps(reps);
            set.setExerciseTypeCode(exerciseCd);
            set.setMinutes(mins);
            set.setHours(hours);
            set.setSeconds(seconds);
        }

        dialog.dismiss();

        DbFunctionObject editSetDfo =
                new DbFunctionObject(set, DbConstants.UPDATE_SET);
        new DbAsyncTask(Constants.CREATE_EDIT_SET).execute(editSetDfo);
    }
}
