package ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

public class MyExpandListener implements RecyclerViewExpandableItemManager.OnGroupExpandListener {
    MyExpandableDraggableSwipeableItemAdapter mAdapter;

    public MyExpandListener(MyExpandableDraggableSwipeableItemAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onGroupExpand(int i, boolean b) {
        mAdapter.isInExpandedState = true;
    }
}
