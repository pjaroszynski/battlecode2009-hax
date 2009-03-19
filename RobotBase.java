package hax;

import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Direction;

import java.util.Random;

public abstract class RobotBase {
    protected final RobotController rc;
    protected final MapLocation startLocation;
    protected Random rand;

    public RobotBase(RobotController rc) {
        this.rc = rc;
        this.startLocation = rc.getLocation();
        rand = new Random(rc.getLocation().hashCode());
        System.out.println(rc.getRobotType().toString() + " @ " + rc.getLocation());
    }

    public void play() throws GameActionException {
        init();
        while (true) {
            try {
                go();
            } catch(Exception e) {
                System.out.println("caught exception:");
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    protected abstract void init() throws GameActionException;

    protected abstract void go() throws GameActionException;

    protected void wander(int chance, int all) throws GameActionException {
        Direction d = rc.getDirection();

        int r = rand.nextInt(all);

        if (rc.canMove(d)) {
            if (r < chance) {
                do {
                    d = d.rotateLeft();
                } while (! rc.canMove(d));
            } else if (r > all - chance - 1) {
                do {
                    d = d.rotateRight();
                } while (! rc.canMove(d));
            }
        } else if (r < all/2) {
            do {
                d = d.rotateLeft();
            } while (! rc.canMove(d));
        } else {
            do {
                d = d.rotateRight();
            } while (! rc.canMove(d));
        }

        if (! rc.getDirection().equals(d)) {
            rc.setDirection(d);
        } else {
            rc.moveForward();
        }
    }
}