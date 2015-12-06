package group.g203.justalittlefit.bus;

import com.squareup.otto.Bus;

/**
 * An Otto event bus for {@link group.g203.justalittlefit.activity.Assign}
 */
public class AssignBus {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
