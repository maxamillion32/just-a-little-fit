package group.g203.justalittlefit.advanced_recyclerview.rv_today;

import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;

public abstract class AbstractExpandableDataProvider {
    public static abstract class BaseData {

        public abstract int getSwipeReactionType();

        public abstract String getText();

        public abstract void setPinnedToSwipeLeft(boolean pinned);

        public abstract boolean isPinnedToSwipeLeft();
    }

    public static abstract class GroupData extends BaseData {
        public abstract boolean isSectionHeader();
        public abstract long getGroupId();
        public abstract Exercise getExercise();
    }

    public static abstract class ChildData extends BaseData {
        public abstract long getChildId();
        public abstract Set getSet();
        public abstract void setExerciseSet(Set set);
        public abstract void setText(String text);
    }

    public abstract int getGroupCount();
    public abstract int getChildCount(int groupPosition);

    public abstract GroupData getGroupItem(int groupPosition);
    public abstract ChildData getChildItem(int groupPosition, int childPosition);
    public abstract void setChildItem(int groupPosition, int childPosition, Set set);

    public abstract void moveGroupItem(int fromGroupPosition, int toGroupPosition);
    public abstract void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);

    public abstract void removeGroupItem(int groupPosition);
    public abstract void removeChildItem(int groupPosition, int childPosition);

    public abstract long undoLastRemoval();
}
