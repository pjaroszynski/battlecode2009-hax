package hax;

import battlecode.common.*;

import java.util.Random;

public class Archon extends RobotBase {
    private State state = State.SEARCH;
    private MapLocation[] archons;
    private MapLocation flux_to_get;
    private int[] staticArchons = new int[6];
    private Robot worker = null;

    enum State {
        GET_NEAREST,
        GET_FLUX,        
        SEARCH,
        FOUND,
        GATHER,
        DONE
    }

    public Archon(RobotController rc) {
        super(rc);
    }

    public void init() {
        archons = rc.senseAlliedArchons();        
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
            case GET_FLUX:
                get_flux();
                break;
            case GET_NEAREST:
                nearest();
                break;
            case FOUND:
                found();
                break;
            case GATHER:
                gather();
                break;
            case DONE:
                return;
        }
    }

    private void updateArchons() {
        MapLocation[] new_archons = rc.senseAlliedArchons();
        for (int i = 0 ; i < new_archons.length ; ++i) {
            if (archons[i].equals(new_archons[i])) {
                ++staticArchons[i];
            } else {
                staticArchons[i] = 0;
            }
        }

        archons = new_archons;
    }

    private void get_flux() throws GameActionException {
        if (rc.getLocation().equals(flux_to_get)) {
            state = State.FOUND;
            return;
        }

        Direction uf = rc.getLocation().directionTo(flux_to_get);

        if (rc.senseAirRobotAtLocation(flux_to_get) != null) {
            state = State.SEARCH;
            return;
        }

        if (! rc.getDirection().equals(uf)) {
            rc.setDirection(uf);
        } else if (rc.canMove(uf)) {
            rc.moveForward();
        }
    }

    private void search() throws GameActionException {
        updateArchons();

        Direction uf = rc.senseDirectionToUnownedFluxDeposit();
        MapLocation ml = rc.getLocation();

        boolean go = true;

        for (MapLocation l : archons) {
            if (ml.directionTo(l).equals(uf)) {
                go = false;
                break;
            }
        }

        if (go) {
            state = State.GET_NEAREST;
            return;
        }

        wander(1, 10);
    }

    void found() throws GameActionException {
        Direction d = rc.getDirection();

        while (rc.senseTerrainTile(rc.getLocation().add(d)).getType() != TerrainTile.TerrainType.LAND) {
            d = d.rotateRight();
        }

        if (! d.equals(rc.getDirection())) {
            rc.setDirection(d);
        } else {
            while (RobotType.WORKER.spawnCost() > rc.getEnergonLevel()) {
                rc.yield();
            }
            rc.spawn(RobotType.WORKER);
            rc.yield();
            state = State.GATHER;            
            worker = rc.senseGroundRobotAtLocation(rc.getLocation().add(rc.getDirection()));
        }
    }

    private void gather() {
        try {
            RobotInfo ri = rc.senseRobotInfo(worker);
            if (rc.getLocation().isAdjacentTo(ri.location) || rc.getLocation().equals(ri.location)) {
                rc.transferEnergon(rc.getEnergonLevel() * 2/3, ri.location, RobotLevel.ON_GROUND);
            }            
        } catch (GameActionException e) {
        }
    }

    void nearest() throws GameActionException {
        Direction uf = rc.senseDirectionToUnownedFluxDeposit();

        if (uf.equals(Direction.NONE)) {
            state = State.DONE;
        } else if (uf.equals(Direction.OMNI)) {
            state = State.FOUND;
        } else if (! rc.getDirection().equals(uf)) {
            rc.setDirection(uf);
        } else if (rc.canMove(uf)) {
            rc.moveForward();
        } else {
            state = State.SEARCH;
        }
    }
}