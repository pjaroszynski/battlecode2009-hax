package hax;

import battlecode.common.*;

import java.util.HashSet;

public class Worker extends RobotBase {
    private MapLocation archon;
    private MapLocation target;
    private HashSet<MapLocation> bad_blocks = new HashSet<MapLocation>();


    public Worker(RobotController rc) {
        super(rc);
        state = State.GET_BACK;
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
        
        //help in DEFENCE
        receiveTargets();
        if(targets_a.size() + targets_g.size() > 2)
        {
        	rc.transform(RobotType.CANNON);
        	RobotBase robot = new Cannon(rc);
        	try {
                robot.play();
            } catch (GameActionException e) {}
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
                if (target == null || (! bad_blocks.contains(l) && archon.distanceSquaredTo(l) < archon.distanceSquaredTo(target)))
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
                bad_blocks.add(target);
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
            bad_blocks.clear();
        } else {
            bad_blocks.add(target);
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