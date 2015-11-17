package ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today;

import android.view.View;
import android.widget.CompoundButton;

import java.util.HashMap;

import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.util.Utils;

public class CbChangeListener implements CompoundButton.OnCheckedChangeListener {

    MyExpandableDraggableSwipeableItemAdapter.MyChildViewHolder viewHolder;
    HashMap<Exercise, View> exerciseViewMap;

    public CbChangeListener(MyExpandableDraggableSwipeableItemAdapter.MyChildViewHolder viewHolder,
                            HashMap<Exercise, View> exerciseViewMap) {
        this.viewHolder = viewHolder;
        this.exerciseViewMap = exerciseViewMap;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.handleStrikeThroughText(viewHolder.mTextView, isChecked);
        viewHolder.set.setComplete(isChecked);
    }
}
