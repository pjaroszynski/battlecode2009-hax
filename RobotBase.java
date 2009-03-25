package hax;

import battlecode.common.*;

import java.util.*;

public abstract class RobotBase {
    protected final RobotController rc;
    protected final MapLocation startLocation;
    protected Random rand;

    protected final HashSet<Robot> allies = new HashSet<Robot>();
    protected final HashSet<Robot> a_nearby = new HashSet<Robot>();
    protected int a_archons = 0;
    protected int a_workers = 0;
    protected int a_soldiers = 0;

    protected final HashSet<Robot> enemies = new HashSet<Robot>();
    protected final HashSet<Robot> e_nearby = new HashSet<Robot>();
    protected int e_archons = 0;
    protected int e_workers = 0;
    protected int e_soldiers = 0;
        
    protected final HashMap<Robot, RobotInfo> r_info = new HashMap<Robot, RobotInfo>();

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

    protected final void sense() throws GameActionException {
        a_nearby.clear();
        a_archons = 0;
        a_workers = 0;
        a_soldiers = 0;

        e_nearby.clear();
        e_archons = 0;
        e_workers = 0;
        e_soldiers = 0;

        doSense(rc.senseNearbyGroundRobots());
        doSense(rc.senseNearbyAirRobots());
    }

    protected final void doSense(Robot[] robots) throws GameActionException {
        for (Robot r : robots) {
            if (rc.canSenseObject(r)) {
                RobotInfo ri = rc.senseRobotInfo(r);
                r_info.put(r, ri);
                if (ri.team != rc.getTeam()) {
                    enemies.add(r);
                    e_nearby.add(r);
                    switch (ri.type) {
                        case ARCHON:
                            ++e_archons;
                            break;
                        case CANNON:
                            break;
                        case CHANNELER:
                            break;
                        case SCOUT:
                            break;
                        case SOLDIER:
                            ++e_soldiers;
                            break;
                        case WORKER:
                            break;
                    }
                } else {
                    allies.add(r);
                    a_nearby.add(r);
                    switch (ri.type) {
                        case ARCHON:
                            ++a_archons;
                            break;
                        case CANNON:
                            break;
                        case CHANNELER:
                            break;
                        case SCOUT:
                            break;
                        case SOLDIER:
                            ++a_soldiers;
                            break;
                        case WORKER:
                            ++a_workers;
                            break;
                    }
                }
            }
        }
    }

    protected final void transfer() throws GameActionException {
        double min = Double.MAX_VALUE;
        Robot minr = null;
        RobotInfo minri = null;

        for (Robot r : a_nearby) {
            RobotInfo ri = r_info.get(r);
            if (ri.location.isAdjacentTo(rc.getLocation()) || ri.location.equals(rc.getLocation())) {
                if (min > ri.energonLevel + ri.energonReserve) {
                    min = ri.energonLevel + ri.energonReserve;
                    minr = r;
                    minri = ri;
                }
            }
        }
        if (minr != null) {
            double e = Math.min(minri.maxEnergon + GameConstants.ENERGON_RESERVE_SIZE - min, GameConstants.ENERGON_RESERVE_SIZE);
            e = Math.min(e, rc.getEnergonLevel() / 2);
            if (e > GameConstants.ENERGON_RESERVE_SIZE / 2) {
                rc.transferEnergon(e, minri.location, minr.getRobotLevel());
            }
        }
    }

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