package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.database.DatabaseHelper;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.dialog.LibraryCreditsDialog;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Home screen activity.
 */
public class Home extends BaseNaviDrawerActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    private DatabaseHelper databaseHelper = null;
    @Bind(R.id.homeLogoText)
    TextView homeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info_libs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_info:
                displayInfoDialog();
                break;
            case R.id.action_libs:
                displayLibsDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.closeAndReleaseDbHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_home, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        setTitle(Constants.EMPTY_STRING);
        getHelper();
        this.formatHomeTextView();
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_home);
        selectedItem.setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_home);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
    }

    /**
     * Method used to get DatabaseHelper object.
     */
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    /**
     * Closes and releases DatabaseHelper object when activity is destroyed.
     */
    private void closeAndReleaseDbHelper() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private void formatHomeTextView() {
            Typeface face=Typeface.createFromAsset(getAssets(), Constants.CUSTOM_FONT_TTF);
            homeTextView.setTypeface(face);
    }

    @OnClick(R.id.todayHomeOption)
    void startTodayActivity() {
        Utils.launchTodayActivity(this);
    }

    @OnClick(R.id.createEditHomeOption)
    void startCreateEditActivity() {
        Intent intent = new Intent(this, CreateEditWorkout.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    @OnClick(R.id.assignHomeOption)
    void startAssignActivity() {
        Intent intent = new Intent(this, Assign.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    @OnClick(R.id.viewHomeOption)
    void startViewWorkoutActivity() {
        Intent intent = new Intent(this, ChooseWorkoutDate.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.HOME);
        dialog.show(fm, getString(R.string.infoDialogTagHome));
    }

    private void displayLibsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        LibraryCreditsDialog dialog = LibraryCreditsDialog.newInstance();
        dialog.show(fm, getString(R.string.libCredDialogTag));
    }
}
