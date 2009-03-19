package hax;

import battlecode.common.*;

public class Worker extends RobotBase {
    private MapLocation archon;
    private MapLocation target;
    private State state = State.GET_BACK;

    enum State {
        SEARCH,
        GET_BLOCK,
        GET_BACK
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
        while(rc.isMovementActive()) {
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
        }
    }

    private void search() throws GameActionException {
        if (rc.getEventualEnergonLevel() < RobotType.WORKER.maxEnergon()) {
            state = State.GET_BACK;
            return;
        }
        MapLocation[] blocks = rc.senseNearbyBlocks();

        for (MapLocation l : blocks) {
            if (l.distanceSquaredTo(archon) > 8) {
                if (target == null || rc.getLocation().distanceSquaredTo(l) < rc.getLocation().distanceSquaredTo(target))
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
        if (! rc.getLocation().isAdjacentTo(target)) {
            if (! rc.getDirection().equals(rc.getLocation().directionTo(target))) {
                rc.setDirection(rc.getLocation().directionTo(target));
                return;
            }
            rc.moveForward();
        } else {
            rc.loadBlockFromLocation(target);
            state = State.GET_BACK;
            target = null;
        }
    }

    private void get_back() throws GameActionException {
        if (! rc.getLocation().isAdjacentTo(archon) && ! rc.getLocation().equals(archon)) {
            if (! rc.getDirection().equals(rc.getLocation().directionTo(archon))) {
                rc.setDirection(rc.getLocation().directionTo(archon));
                return;
            }

            if (rc.canMove(rc.getDirection())) {
                rc.moveForward();
            } else {
                rc.moveBackward();
                unload(rc.getLocation());
            }
        } else {
            while (rc.getNumBlocks() > 0) {
                if (rc.canUnloadBlockToLocation(archon)) {
                    rc.unloadBlockToLocation(archon);
                } else {
                    Direction d = rc.getDirection();

                    while (! rc.canMove(d)) {
                        d.rotateRight();
                    }
                    if (d != rc.getDirection()) {
                        rc.setDirection(d);
                        rc.yield();
                    }

                    rc.moveForward();
                    unload(rc.getLocation());
                }
            }
            while (rc.getEventualEnergonLevel() < RobotType.WORKER.maxEnergon()) {
                rc.yield();
            }
            state = State.SEARCH;
        }
    }

    private void unload(MapLocation l) throws GameActionException {
        while (! rc.canUnloadBlockToLocation(l) || rc.getCurrentAction() != ActionType.IDLE) {
            rc.yield();
        }
        rc.unloadBlockToLocation(l);
    }
}