package it.unipi.gio.vaseDriver;

import it.unipi.gio.vaseDriver.model.Goal;
import it.unipi.gio.vaseDriver.rest.out.GioPlants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GoalLogic implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GoalLogic.class);

    private GioPlants vase;

    private CopyOnWriteArrayList<Goal> goalList;

    public GoalLogic(CopyOnWriteArrayList<Goal> goalList, GioPlants vase){
        this.goalList = goalList;
        this.vase = vase;
        //autostart
        new Thread(this).start();
    }

    @Override
    public void run() {
        while(true){
            if (!goalList.isEmpty()) {
                for (Goal g : goalList) {
                    if (g.inTimeInterval()) {
                        checkMoisture(g);
                    }
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void checkMoisture(Goal g){
        Float topMoisture = g.getMoistureTop();
        Float bottomMoisture = g.getMoistureBottom();
        Float realMoisture = vase.getMoistureValue();
        if(realMoisture==null){
            return;
        }
        if(realMoisture<bottomMoisture){
            vase.watering();
        }

    }
}
