package ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.HashMap;

import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class CbChangeListener implements CompoundButton.OnCheckedChangeListener {

    MyExpandableDraggableSwipeableItemAdapter.MyChildViewHolder viewHolder;
    HashMap<Exercise, View> exerciseViewMap;
    HashMap<Exercise, Integer> exerciseCountMap;

    public CbChangeListener(MyExpandableDraggableSwipeableItemAdapter.MyChildViewHolder viewHolder,
                            HashMap<Exercise, View> exerciseViewMap,
                            HashMap<Exercise, Integer> exerciseCountMap) {
        this.viewHolder = viewHolder;
        this.exerciseViewMap = exerciseViewMap;
        this.exerciseCountMap = exerciseCountMap;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int exerciseSetCount = exerciseCountMap.get(viewHolder.set.getExercise());
        TextView tv = (TextView) exerciseViewMap.get(viewHolder.set.getExercise());

        if (isChecked) {
            Utils.strikeThroughText(viewHolder.mTextView);
            exerciseCountMap.put(viewHolder.set.getExercise(), Utils.returnExerciseSetCount(viewHolder.set.getExercise(),
                    exerciseSetCount, Constants.INT_NEG_ONE));
        } else {
            Utils.clearStrikeThroughText(viewHolder.mTextView);
            exerciseCountMap.put(viewHolder.set.getExercise(), Utils.returnExerciseSetCount(viewHolder.set.getExercise(),
                    exerciseSetCount, Constants.INT_ONE));
        }

        if (exerciseCountMap.get(viewHolder.set.getExercise()) == Constants.INT_ZERO) {
            Utils.strikeThroughText(tv);
            viewHolder.set.getExercise().setComplete(true);
        } else {
            Utils.clearStrikeThroughText(tv);
            viewHolder.set.getExercise().setComplete(false);
        }

        viewHolder.set.setComplete(isChecked);
    }
}
