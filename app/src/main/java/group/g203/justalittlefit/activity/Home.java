package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.database.DatabaseHelper;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.dialog.LibraryCreditsDialog;
import group.g203.justalittlefit.util.Constants;

/**
 * Home screen activity.
 */
public class Home extends BaseActivity {
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
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        handleBottomNaviDisplay(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
