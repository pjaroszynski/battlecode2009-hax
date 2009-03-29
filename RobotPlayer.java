package hax;

import battlecode.common.*;

public class RobotPlayer implements Runnable {

   private RobotBase robot;

    public RobotPlayer(RobotController rc) {
        switch (rc.getRobotType()) {
            case ARCHON:
                robot = new Archon(rc);
                break;
            case WORKER:
                robot = new Worker(rc);
                break;
            case SOLDIER:
                robot = new Soldier(rc);
                break;
            default:
                System.out.print("No robot for " + rc.getRobotType().toString());
        }
    }

   public void run() {
       try {
           robot.play();
       } catch (GameActionException e) {
           
       }
   }
}