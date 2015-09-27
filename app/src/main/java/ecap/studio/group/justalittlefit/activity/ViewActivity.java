package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.dialog.InformationDialog;
import ecap.studio.group.justalittlefit.util.Constants;

public class ViewActivity extends BaseNaviDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final BaseNaviDrawerActivity activity = this;
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_view, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(R.string.view_title_string);
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_view);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.VIEW_TEXT);
        dialog.show(fm, getString(R.string.infoDialogTagExercise));
    }
}
