package group.g203.justalittlefit.bus;

import com.squareup.otto.Bus;

/**
 * An Otto event bus for {@link group.g203.justalittlefit.activity.ViewActivity}
 */
public class ViewBus {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
