package ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today;

import android.widget.CompoundButton;

import ecap.studio.group.justalittlefit.util.Utils;

public class CbChangeListener implements CompoundButton.OnCheckedChangeListener {

    MyExpandableDraggableSwipeableItemAdapter.MyChildViewHolder viewHolder;

    public CbChangeListener(MyExpandableDraggableSwipeableItemAdapter.MyChildViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.handleStrikeThroughText(viewHolder.mTextView, isChecked);
    }
}
