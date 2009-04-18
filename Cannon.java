package hax;

import java.util.HashSet;

import battlecode.common.*;

public class Cannon extends RobotBase {
    private State state = State.ATTACK_A;

    public Cannon(RobotController rc) {
        super(rc);
    }

    enum State {
        ATTACK_G,
        ATTACK_A
    }
 

    protected void init() throws GameActionException {
    }

    protected void go() throws GameActionException {
        rc.setIndicatorString(0, state.toString());
        while (rc.isMovementActive()) {
            rc.yield();
        }
        
        if (Clock.getRoundNum() % 3 == 0) {
            transfer();
        }
        
        Position pos = receiveTargets();
        if (pos == Position.NONE) {
        	wander(2, 10); 	
        	return;
        }
        	
        switch(pos)
        {
        	case AIR:
        		state = State.ATTACK_A;
        		break;
        	case GROUND:
        		state = State.ATTACK_G;
        		break;
        	case BOTH:
        		state = State.ATTACK_A; //air priority
        		break;
        }


        switch (state) {
            case ATTACK_G:
                attack(Position.GROUND, targets_g);
                break;
            case ATTACK_A:
                attack(Position.AIR, targets_a);
                break;
        }
    }

}