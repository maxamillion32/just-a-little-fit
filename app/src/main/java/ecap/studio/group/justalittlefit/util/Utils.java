package ecap.studio.group.justalittlefit.util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
}
