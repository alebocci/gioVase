package it.unipi.gio.vaseDriver.model;

import java.time.LocalTime;

public class Goal {

    private int startHour=-1;
    private int stopHour=-1;
    private Float moistureBottom;
    private Float moistureTop;


    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        if(startHour<0 || startHour>23) {startHour=-1;}
        this.startHour = startHour;
    }

    public int getStopHour() {
        return stopHour;
    }

    public void setStopHour(int stopHour) {
        if(stopHour<0 || stopHour>23) {stopHour=-1;}
        this.stopHour = stopHour;
    }

    public Float getMoistureBottom() {
        return moistureBottom;
    }

    public void setMoistureBottom(Float moistureBottom) {
        this.moistureBottom = moistureBottom;
    }

    public Float getMoistureTop() {
        return moistureTop;
    }

    public void setMoistureTop(Float moistureTop) {
        this.moistureTop = moistureTop;
    }

    private boolean hourSet(){return startHour!=-1 || stopHour!=-1;}

    public boolean inTimeInterval(){
        if (!hourSet()){return true;}
        int tempStop = stopHour;
        int hourNow = LocalTime.now().getHour();
        if(startHour==-1){return hourNow<stopHour;}
        if(stopHour==-1){return hourNow>startHour;}
        if(startHour > stopHour){
            tempStop +=24;
            hourNow +=24;
        }
        return (startHour <= hourNow && hourNow < tempStop);
    }
}
