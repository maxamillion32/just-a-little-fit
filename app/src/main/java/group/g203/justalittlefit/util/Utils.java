package group.g203.justalittlefit.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.activity.Assign;
import group.g203.justalittlefit.activity.ChooseWorkoutDate;
import group.g203.justalittlefit.activity.CreateEditWorkout;
import group.g203.justalittlefit.activity.Home;
import group.g203.justalittlefit.fragment.PeekLauncher;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;
import group.g203.justalittlefit.model.Workout;

/**
 * Class that houses common utility methods needed by app.
 */
public class Utils {
    public static String ensureValidString(String text) {
        if (text == null || text.isEmpty()) {
            return Constants.EMPTY_STRING;
        } else {
            return text.trim();
        }
    }

    public static boolean collectionIsNullOrEmpty(Collection collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmptyString(String string) {
        return string.trim().isEmpty();
    }

    public static void displayLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void displayShortToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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

    public static void setupDrawerContent(final AppCompatActivity activity, final DrawerLayout drawerLayout, NavigationView navigationView) {
        TextView headerView = new TextView(activity);
        headerView.setGravity(Gravity.CENTER);
        headerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
        Typeface face=Typeface.createFromAsset(activity.getAssets(), Constants.CUSTOM_FONT_TTF);
        headerView.setText(activity.getString(R.string.title_name));
        headerView.setTypeface(face);
        headerView.setPadding(0, 80, 0, 0);
        navigationView.addHeaderView(headerView);

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
                        } else if (selectedTitle.equals(activity.getString(R.string.home_string).trim())) {
                            Intent homeIntent =
                                    new Intent(activity, Home.class);
                            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            activity.startActivity(homeIntent);
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
        PeekLauncher launcher = (PeekLauncher) activity.getSupportFragmentManager()
                .findFragmentByTag(Constants.PEEK_LAUNCHER_FRAG_TAG);
        if (launcher != null) {
            activity.getSupportFragmentManager().beginTransaction().remove(launcher).commitAllowingStateLoss();
        }
        launcher = PeekLauncher.getNewInstance(null);
        activity.getSupportFragmentManager().executePendingTransactions();
        activity.getSupportFragmentManager().beginTransaction().add(launcher, Constants.PEEK_LAUNCHER_FRAG_TAG).commitAllowingStateLoss();
    }

    public static void launchViewActivity(AppCompatActivity activity, DateTime dateTime) {
        PeekLauncher launcher = (PeekLauncher) activity.getSupportFragmentManager()
                .findFragmentByTag(Constants.PEEK_LAUNCHER_FRAG_TAG);
        if (launcher != null) {
            activity.getSupportFragmentManager().beginTransaction().remove(launcher).commitAllowingStateLoss();
        }
        launcher = PeekLauncher.getNewInstance(dateTime);
        activity.getSupportFragmentManager().executePendingTransactions();
        activity.getSupportFragmentManager().beginTransaction().add(launcher, Constants.PEEK_LAUNCHER_FRAG_TAG).commitAllowingStateLoss();
    }

    public static ProgressDialog showProgressDialog(Context context) {
        ProgressDialog progressDialog = ProgressDialog.show(context, Constants.EMPTY_STRING, Constants.LOADING);
        progressDialog.setCancelable(true);
        return progressDialog;
    }

    public static String returnStandardDateString(DateTime dateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(Constants.STANDARD_DATE_PATTERN);
        return dateTimeFormatter.print(dateTime);
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    public static void strikeThroughText(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public static void clearStrikeThroughText(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
    }

    public static boolean isToday(DateTime dateTime) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        DateTime startOfToday = today.toDateTimeAtStartOfDay();
        DateTime startOfTomorrow = tomorrow.toDateTimeAtStartOfDay();

        Interval todayInterval = new Interval(startOfToday, startOfTomorrow);
        return todayInterval.contains(dateTime);
    }

    public static Integer returnExerciseSetCount(Exercise exercise, int currentCount, int addend) {
        int maxCount = exercise.getSets().size();
        int currentPlusAddend = currentCount + addend;

        if (currentPlusAddend > maxCount) {
            return maxCount;
        } else {
            return currentPlusAddend;
        }
    }

    public static boolean isWorkoutComplete(Workout workout) {
        boolean workoutIsComplete = true;
        for (Exercise exercise : workout.getExercises()) {
            if (!exercise.isComplete()) {
                workoutIsComplete = false;
                break;
            }
        }
        return workoutIsComplete;
    }

    public static boolean editableIsZeroOrNullOrEmpty(Editable value) {
        if (value == null || value.toString() == null || value.toString().isEmpty()
                || Integer.parseInt(value.toString().trim()) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static Integer ensureNonNullInteger(Integer intObj) {
        if (intObj == null) {
            return 0;
        } else {
            return intObj;
        }
    }

    public static String returnTwoDigitString(String digits) {
        String validDigits = ensureValidString(digits).trim();

        if (validDigits.length() < 2) {
            return "0" + validDigits;
        } else {
            return digits;
        }
    }

    public static Integer removeWorkouts(CloseableIterator<Workout> iterator, HashSet<Workout> workouts) {
        Integer removedWorkout = 0;
        try {
            while (iterator.hasNext()) {
                Workout workout = iterator.next();
                if (workouts.contains(workout)) {
                    iterator.remove();
                }
            }
        } finally {
            try {
                iterator.close();
            } catch (SQLException e) {
                removedWorkout = Constants.INT_NEG_ONE;
            }
        }
        return removedWorkout;
    }

    public static Integer removeExercise(ForeignCollection<Exercise> exercises, String exerciseName) {
        Integer removedExercise = 0;
        CloseableIterator<Exercise> iterator =
                exercises.closeableIterator();
        try {
            while (iterator.hasNext()) {
                Exercise exercise = iterator.next();
                if (ensureValidString(exercise.getName()).trim().equals(exerciseName)) {
                    iterator.remove();
                    removedExercise = Constants.INT_ONE;
                }
            }
        } finally {
            try {
                iterator.close();
            } catch (SQLException e) {
                removedExercise = Constants.INT_NEG_ONE;
            }
        }
        return removedExercise;
    }
    public static Integer removeSet(ForeignCollection<Set> sets, String exerciseName) {
        Integer removedSets = 0;
        CloseableIterator<Set> iterator =
                sets.closeableIterator();
        try {
            while (iterator.hasNext()) {
                Set set = iterator.next();
                if (ensureValidString(set.toString()).trim().equals(exerciseName)) {
                    iterator.remove();
                    removedSets = Constants.INT_ONE;
                }
            }
        } finally {
            try {
                iterator.close();
            } catch (SQLException e) {
                removedSets = Constants.INT_NEG_ONE;
            }
        }
        return removedSets;
    }

    public static Map<String, Workout> makeNameWorkoutMap(List<Workout> workouts) {
        if (!Utils.collectionIsNullOrEmpty(workouts)) {
            HashMap<String, Workout> map = new HashMap<>(workouts.size());

            for (Workout workout : workouts) {
                map.put(ensureValidString(workout.getName()), workout);
            }

            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    public static List<String> getWorkoutNameList(List<Workout> workouts) {
        if (!Utils.collectionIsNullOrEmpty(workouts)) {
            List<String> names = new ArrayList<>(workouts.size());

            for (Workout workout : workouts) {
                names.add(ensureValidString(workout.getName()));
            }
            return names;
        } else {
            return Collections.emptyList();
        }
    }

    public static String returnTodayEditSetErrorString(Set set) {
        String exerciseTypeCode = ensureValidString(set.getExerciseTypeCode());
        String errMsg = Constants.EMPTY_STRING;
        if (exerciseTypeCode.equals(Constants.WEIGHTS)) {
            if (set.getReps() == 0) {
                errMsg += "Please enter in a rep count greater than 0";
            }
            if (ensureNonNullInteger(set.getWeight()) == 0) {
                String weightErr = "Please enter in a weight amount greater than 0";
                if (errMsg.trim().isEmpty()) {
                    errMsg += weightErr;
                } else {
                    errMsg += "\n" + weightErr;
                }
            }
        } else if (exerciseTypeCode.equals(Constants.LOGGED_TIMED)) {
            if (set.getReps() == 0) {
                errMsg += "Please enter in a rep count greater than 0";
            }
            if (ensureNonNullInteger(set.getHours()) == 0 &&
                    ensureNonNullInteger(set.getMinutes()) == 0 &&
                    ensureNonNullInteger(set.getSeconds()) == 0) {
                String timedErr = "Please enter in at least one value for hours, minutes, or seconds";
                if (errMsg.trim().isEmpty()) {
                    errMsg += timedErr;
                } else {
                    errMsg += "\n" + timedErr;
                }
            }
        }
        return errMsg;
    }
}
