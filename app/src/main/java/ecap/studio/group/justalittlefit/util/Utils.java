package ecap.studio.group.justalittlefit.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.activity.Assign;
import ecap.studio.group.justalittlefit.activity.ChooseWorkoutDate;
import ecap.studio.group.justalittlefit.activity.CreateEditWorkout;
import ecap.studio.group.justalittlefit.fragment.TodayLauncher;
import ecap.studio.group.justalittlefit.model.Workout;

/**
 * Class that houses common utility methods needed by app.
 */
public class Utils {
    public static String ensureValidString(String text) {
        if (text == null || text.isEmpty()) {
            return Constants.EMPTY_STRING;
        } else {
            return text;
        }
    }

    public static boolean isCurrentActivity(String currentActivity, String requestedActivity) {
        String trimmedCurrentActivity =
                Utils.ensureValidString((currentActivity)).trim();
        String trimmedRequestedActivity =
                Utils.ensureValidString((requestedActivity)).trim();
        return (trimmedCurrentActivity.equals(trimmedRequestedActivity));
    }

    public static boolean collectionIsNullOrEmpty(Collection collection) {
        return (collection == null || collection.isEmpty());
    }

    public static void displayLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void displayShortToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean androidIsLessThanThisVersion(int version) {
        if (Constants.ANDROID_SDK_INT >= version) {
            return false;
        } else {
            return true;
        }
    }

    public static int getNumberOfDigits(int number) {
        return (int)Math.floor(Math.log10(number) + 1);
    }

    public static void reorderWorkouts(List<Workout> workouts) {
        Iterator<Workout> iterator = workouts.iterator();

        while(iterator.hasNext()) {
            Workout w = iterator.next();
            w.setOrderNumber(workouts.indexOf(w));
        }
    }

    public static ArrayList<DateTime> dateListToDateTimeList(List<Date> dates) {
        ArrayList<DateTime> dateTimes = new ArrayList<>();
        for (Date date : dates) {
            dateTimes.add(new DateTime(date));
        }
        return dateTimes;
    }

    public static void displayLongSimpleSnackbar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void displayShortSimpleSnackbar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void displayLongActionSnackbar(View view, String msg,
                                                 String actionText,
                                                 View.OnClickListener listener, int colorInt) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction(actionText, listener)
                .setActionTextColor(colorInt).show();
    }
    public static void displayShortActionSnackbar(View view, String msg,
                                                  String actionText,
                                                  View.OnClickListener listener, int colorInt) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
                .setAction(actionText, listener)
                .setActionTextColor(colorInt).show();
    }

    public static int returnValidNumberFromEditText(EditText editText) {
        String text = editText.getText().toString();
        if (Utils.ensureValidString(text) == Constants.EMPTY_STRING) {
            return 0;
        } else {
            return Integer.parseInt(text.toString());
        }
    }

    public static void startViewWorkoutActivity(Context context, ArrayList<Workout> workouts,
                                         boolean isViewOnly) {
        //todo, change Assign.class to real activity
        Intent intent = new Intent(context, Assign.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.WORKOUTS, workouts);
        bundle.putBoolean(Constants.VIEW_ONLY, isViewOnly);

        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void setupDrawerContent(final AppCompatActivity activity, final DrawerLayout drawerLayout, NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        String activityTitle = activity.getTitle().toString().trim();
                        String selectedTitle = menuItem.getTitle().toString().trim();

                        if (selectedTitle.equals(activityTitle)) {
                             /* do nothing, menu item will be checked and close out drawer after
                             the completion of this if-else logic */
                        } else if (selectedTitle.equals(activity.getString(R.string.today_string).trim())) {
                            launchTodayActivity(activity);
                        } else if (selectedTitle.equals(activity.getString(R.string.create_edit_string).trim())) {
                            Intent createEditWorkoutIntent =
                                    new Intent(activity, CreateEditWorkout.class);
                            createEditWorkoutIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            activity.startActivity(createEditWorkoutIntent);
                        } else if (selectedTitle.equals(activity.getString(R.string.assign_string).trim())) {
                            Intent assignIntent =
                                    new Intent(activity, Assign.class);
                            assignIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            activity.startActivity(assignIntent);
                        } else if (selectedTitle.equals(activity.getString(R.string.view_string).trim())) {
                            Intent chooseWorkoutDateIntent =
                                    new Intent(activity, ChooseWorkoutDate.class);
                            chooseWorkoutDateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            activity.startActivity(chooseWorkoutDateIntent);
                        } else {
                            /* Shouldn't reach this but if so, doing nothing here
                            is harmless as the drawer will close */
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    public static void launchTodayActivity(AppCompatActivity activity) {
        TodayLauncher launcher = (TodayLauncher) activity.getSupportFragmentManager()
                .findFragmentByTag(Constants.TODAY_LAUNCHER_FRAG_TAG);
        if (launcher == null) {
            launcher = TodayLauncher.getNewInstance();
        } else {
            activity.getSupportFragmentManager().beginTransaction().remove(launcher).commitAllowingStateLoss();
        }
        activity.getSupportFragmentManager().executePendingTransactions();
        activity.getSupportFragmentManager().beginTransaction().add(launcher, Constants.TODAY_LAUNCHER_FRAG_TAG).commitAllowingStateLoss();
    }
}
