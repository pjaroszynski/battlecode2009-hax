package hax;

import battlecode.common.*;

public class Soldier extends RobotBase {
    private State state = State.SEARCH;

    public Soldier(RobotController rc) {
        super(rc);
    }

    enum State {
        ATTACK,
        SEARCH
    }

    protected void init() throws GameActionException {
    }

    protected void go() throws GameActionException {
        rc.setIndicatorString(0, state.toString());
        while (rc.isMovementActive()) {
            rc.yield();
        }

        sense();

        if (Clock.getRoundNum() % 5 == 0) {
            transfer();
        }

        switch (state) {
            case SEARCH:
                search();
                break;
        }
    }

    private void search() throws GameActionException {
        RobotInfo target = null;
        int dist = Integer.MAX_VALUE;
        for (Robot r : e_nearby) {
            RobotInfo ri = r_info.get(r);
            if (dist > ri.location.distanceSquaredTo(rc.getLocation())) {
                dist = ri.location.distanceSquaredTo(rc.getLocation());
                target = ri;
            }
        }

        if (target == null) {
            wander(2, 10);
        } else if (rc.canAttackSquare(target.location)) {
            if (rc.isAttackActive()) {
                return;
            }
            if (target.type.isAirborne()) {
                rc.attackAir(target.location);
            } else {
                rc.attackGround(target.location);
            }
        } else if (rc.getLocation().directionTo(target.location) != rc.getDirection()) {
            rc.setDirection(rc.getLocation().directionTo(target.location));
        } else if (rc.canMove(rc.getDirection())) {
            rc.moveForward();
        }
    }
}