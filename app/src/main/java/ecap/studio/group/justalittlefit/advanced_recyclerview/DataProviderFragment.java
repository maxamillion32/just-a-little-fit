package ecap.studio.group.justalittlefit.advanced_recyclerview;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.List;

import ecap.studio.group.justalittlefit.bus.DataProviderBus;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class DataProviderFragment extends Fragment {
    private AbstractDataProvider mDataProvider;
    private String dataType;

    public static DataProviderFragment newInstance(String dataType) {
        DataProviderFragment fragment = new DataProviderFragment();
        Bundle args = new Bundle(Constants.INT_ONE);
        args.putString(Constants.DATA_FRAG_TYPE, dataType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        DataProviderBus.getInstance().register(this);

        Bundle args = getArguments();

        if (args != null) {
            dataType = args.getString(Constants.DATA_FRAG_TYPE);
            switch (dataType) {
                case Constants.WORKOUT:
                    mDataProvider = new DataProvider(dataType);
                    break;
            }
        } else {
            Utils.displayLongToast(this.getActivity(), "Error getting DataProviderFragment");
        }
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }
}