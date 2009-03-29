package hax;

import battlecode.common.*;

public class Worker extends RobotBase {
    private MapLocation archon;
    private MapLocation target;
    private State state = State.GET_BACK;

    enum State {
        SEARCH,
        GET_BLOCK,
        GET_BACK,
        UNLOAD
    }

    public Worker(RobotController rc) {
        super(rc);
    }

    protected void init() throws GameActionException {
        for (MapLocation l : rc.senseAlliedArchons()) {
            if (l.isAdjacentTo(rc.getLocation()) && rc.senseFluxDepositAtLocation(l) != null) {
                archon = l;
                break;
            }
        }
    }

    protected void go() throws GameActionException {
        rc.setIndicatorString(0, state.toString());
        while (rc.isMovementActive()) {
            rc.yield();
        }
        switch (state) {
            case SEARCH:
                search();
                break;
            case GET_BACK:
                get_back();
                break;
            case GET_BLOCK:
                get_block();
                break;
            case UNLOAD:
                unload();
                break;
        }
    }

    private void search() throws GameActionException {
        if (rc.getEventualEnergonLevel() < RobotType.WORKER.maxEnergon() / 2) {
            state = State.GET_BACK;
            return;
        }
        MapLocation[] blocks = rc.senseNearbyBlocks();

        for (MapLocation l : blocks) {
            if (l.distanceSquaredTo(archon) > 4) {
                if (target == null || (l != target && archon.distanceSquaredTo(l) < archon.distanceSquaredTo(target)))
                    target = l;
            }
        }

        if (target != null) {
            state = State.GET_BLOCK;
            return;
        }

        wander(1, 10);
    }

    private void get_block() throws GameActionException {
        switch (moveToAdjacent(target)) {
            case BLOCKED:
                state = State.SEARCH;
                return;
            case DONE:
                break;
            case MOVING:
                return;
        }
        if (rc.canLoadBlockFromLocation(target)) {
            rc.loadBlockFromLocation(target);
            state = State.GET_BACK;
            target = null;
        } else {
            state = State.SEARCH;
        }
    }

    private void get_back() throws GameActionException {
        switch (moveToAdjacent(archon)) {
            case BLOCKED:
                target = rc.getLocation();
                state = State.UNLOAD;
                break;
            case DONE:
                target = archon;
                state = State.UNLOAD;
                break;
            case MOVING:
                return;
        }
    }

    private void unload() throws GameActionException {
        if (rc.getNumBlocks() > 0) {
            switch (moveToAdjacent(target)) {
                case BLOCKED:
                    rc.suicide();
                    return;
                case DONE:
                    break;
                case MOVING:
                    return;
            }
            if (! rc.canUnloadBlockToLocation(target)) {
                target = rc.getLocation();
                return;
            }

            while (rc.getCurrentAction() != ActionType.IDLE) {
                rc.yield();
            }
            rc.unloadBlockToLocation(target);
        }
        target = null;
        state = State.SEARCH;
    }
}