package dev.westernpine.pulse.sources;

import com.sedmelluq.lava.extensions.youtuberotator.planner.*;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes") //IpBlocks
public enum Router {

    //Rotate to new IP when banned
    ROTATE_ON_BAN("rotateonban") {
        @Override
        public AbstractRoutePlanner getRouter(List<IpBlock> ipBlocks) {
            return new RotatingIpRoutePlanner(ipBlocks);
        }
    },

    //Random address each request
    LOAD_BALANCE("loadbalance") {
        @Override
        public AbstractRoutePlanner getRouter(List<IpBlock> ipBlocks) {
            return new BalancingIpRoutePlanner(ipBlocks);
        }
    },

    //Requires /64, ip = current nano-time offset
    NANO_SWITCH("nanoswitch") {
        @Override
        public AbstractRoutePlanner getRouter(List<IpBlock> ipBlocks) {
            return new NanoIpRoutePlanner(ipBlocks, true);
        }
    },
    //Combination of NANO_SWITCH(NanoIpRoutePlanner) & LOAD_BALANCE(RotatingIpRoutePlanner)
    ROTATING_NANO_SWITCH("rotatingnanoswitch") {
        @Override
        public AbstractRoutePlanner getRouter(List<IpBlock> ipBlocks) {
            return new RotatingNanoIpRoutePlanner(ipBlocks);
        }
    },
    ;

    private String label;

    Router(String label) {
        this.label = label;
    }

    public abstract AbstractRoutePlanner getRouter(List<IpBlock> ipBlocks);

    public static Optional<Router> from(String identifier) {
        return Arrays.asList(Router.values()).stream().filter(router -> router.getLabel().equalsIgnoreCase(identifier) || router.toString().equalsIgnoreCase(identifier)).findAny();
    }

    public String getLabel() {
        return label;
    }

}