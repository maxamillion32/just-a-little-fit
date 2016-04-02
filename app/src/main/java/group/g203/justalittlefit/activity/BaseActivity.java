package group.g203.justalittlefit.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Base activity from which all other app activities derive.
 */
public class BaseActivity extends AppCompatActivity {
    FrameLayout frameLayout;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(Constants.LOADING);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void startTodayActivity(View view) {
        Utils.launchTodayActivity(this);
    }

    public void startCreateEditActivity(View view) {
        Intent intent = new Intent(this, CreateEditWorkout.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    public void startAssignActivity(View view) {
        Intent intent = new Intent(this, Assign.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    public void startViewWorkoutActivity(View view) {
        Intent intent = new Intent(this, ChooseWorkoutDate.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    public void handleBottomNaviDisplay(boolean showBottomOnly) {
        CoordinatorLayout clBase = (CoordinatorLayout) findViewById(R.id.clBase);
        LinearLayout llOfClBase = (LinearLayout) findViewById(R.id.llOfClBase);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (showBottomOnly) {
            clBase.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
            llOfClBase.setVisibility(View.VISIBLE);

        } else {
            clBase.setVisibility(View.VISIBLE);
        }
    }

    public void handleNaviSelectionColor(String naviCase) {
        ImageView icon = null;
        TextView textView = null;
        int blueGrayColor = ContextCompat.getColor(this, R.color.app_blue_gray);
        int grayColor = ContextCompat.getColor(this, R.color.app_gray);
        switch (naviCase) {
            case Constants.TODAY:
                icon = (ImageView) findViewById(R.id.todayBottomIcon);
                textView = (TextView) findViewById(R.id.todayBottomText);

                ((ImageView)findViewById(R.id.viewBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.viewBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.assignBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.assignBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.createEditBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.createEditBottomText)).setTextColor(grayColor);
                break;
            case Constants.ASSIGN:
                icon = (ImageView) findViewById(R.id.assignBottomIcon);
                textView = (TextView) findViewById(R.id.assignBottomText);

                ((ImageView)findViewById(R.id.viewBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.viewBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.todayBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.todayBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.createEditBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.createEditBottomText)).setTextColor(grayColor);
                break;
            case Constants.VIEW:
                icon = (ImageView) findViewById(R.id.viewBottomIcon);
                textView = (TextView) findViewById(R.id.viewBottomText);

                ((ImageView)findViewById(R.id.todayBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.todayBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.assignBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.assignBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.createEditBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.createEditBottomText)).setTextColor(grayColor);
                break;
            case Constants.CREATE_EDIT:
                icon = (ImageView) findViewById(R.id.createEditBottomIcon);
                textView = (TextView) findViewById(R.id.createEditBottomText);

                ((ImageView)findViewById(R.id.viewBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.viewBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.assignBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.assignBottomText)).setTextColor(grayColor);
                ((ImageView)findViewById(R.id.todayBottomIcon)).setColorFilter(grayColor);
                ((TextView)findViewById(R.id.todayBottomText)).setTextColor(grayColor);
                break;
        }
        if (icon != null) {
            icon.setColorFilter(blueGrayColor);
        }
        if (textView != null) {
            textView.setTextColor(blueGrayColor);
        }
    }
}


