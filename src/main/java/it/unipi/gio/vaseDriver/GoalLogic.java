package it.unipi.gio.vaseDriver;

import it.unipi.gio.vaseDriver.model.GioPlantsResponse;
import it.unipi.gio.vaseDriver.model.Goal;
import it.unipi.gio.vaseDriver.rest.out.GioPlants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GoalLogic implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GoalLogic.class);

    private GioPlants vase;
    private Goal goal;

    private AtomicBoolean goalActive;
    private String urlToPing;

    private RestTemplate restTemplate;

    public GoalLogic(GioPlants vase, RestTemplate restTemplate){
        this.vase = vase;
        goalActive = new AtomicBoolean(true);
        this.restTemplate=restTemplate;
        //autostart
        new Thread(this).start();
    }

    @Override
    public void run() {
        int secondSleep;
        int pings=0;
        while(true){
            if(goalActive.get()) {
                Goal g = getGoal();
                if (g != null) {
                    if (g.inTimeInterval()) {
                        checkMoisture(g);
                    }
                }
                secondSleep = 120;
            }else{
                if(!checkConnectionAlive()) {
                    if (++pings == 3) {
                        activateGoals();
                        pings=0;
                    }
                }else {
                      pings = 0;
                }

                secondSleep = 60;
            }
            try {
                Thread.sleep(10*1000);
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

    public synchronized Goal getGoal() {
        return goal;
    }

    public synchronized boolean setGoal(Goal goal) {
        if(goalActive.get()) {
            this.goal = goal;
            return true;
        }
        return false;
    }

    public synchronized void activateGoals() {
        goalActive.set(true);
        this.urlToPing=null;
    }

    public synchronized boolean disactivateGoals(String urlToPing) {
        if(this.urlToPing!=null){
            return false;
        }
        goalActive.set(false);
        this.urlToPing=urlToPing;
        return true;
    }

    private boolean checkConnectionAlive(){
        LOG.info("Ping service above at "+urlToPing+"/api/goals/ping");
        ResponseEntity<Void> response;
        try {
            response =  restTemplate.getForEntity(urlToPing+"/api/goals/ping", Void.class);
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return false;
        }
        return response.getStatusCode().is2xxSuccessful();
    }
}
