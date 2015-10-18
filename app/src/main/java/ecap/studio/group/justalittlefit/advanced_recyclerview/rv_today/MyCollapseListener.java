package ecap.studio.group.justalittlefit.advanced_recyclerview.rv_today;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

public class MyCollapseListener implements RecyclerViewExpandableItemManager.OnGroupCollapseListener {
    MyExpandableDraggableSwipeableItemAdapter mAdapter;

    public MyCollapseListener(MyExpandableDraggableSwipeableItemAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onGroupCollapse(int i, boolean b) {
        mAdapter.isInExpandedState = false;
    }
}
