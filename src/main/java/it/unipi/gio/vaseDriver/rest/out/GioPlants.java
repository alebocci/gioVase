package it.unipi.gio.vaseDriver.rest.out;

import it.unipi.gio.vaseDriver.model.GioPlantsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GioPlants {

    private static final Logger LOG = LoggerFactory.getLogger(GioPlants.class);


    private InetAddress ip;
    private int port;
    private String baseAddress;
    private RestTemplate restTemplate;

    public GioPlants(InetAddress ip, int port){
        if(ip==null) return;
        this.ip = ip;
        this.port = port;
        this.restTemplate = new RestTemplate();
        connectToGioPlants();

    }

    private boolean connectToGioPlants(){
        baseAddress = "http://"+ip.getHostName()+":"+port+"/rooms/";
        int fail = 0;
        do {
            String roomId = "";
            String deviceId = "";
            try {
                ResponseEntity<GioPlantsResponse[]> response = restTemplate.getForEntity(baseAddress, GioPlantsResponse[].class);
                if (response == null || response.getBody() == null || response.getBody().length == 0) {
                    if(++fail==3){
                        baseAddress="http://localhost:0";
                        return false;
                    }
                    continue;
                }
                roomId = response.getBody()[0].getId();
                response = restTemplate.getForEntity(baseAddress + roomId + "/devices", GioPlantsResponse[].class);
                if (response == null || response.getBody() == null || response.getBody().length == 0) {
                    if(++fail==3){
                        baseAddress="http://localhost:0";
                        return false;
                    }
                    continue;
                }
                deviceId = response.getBody()[0].getId();
                if (deviceId == null) throw new IllegalArgumentException();
            } catch (NullPointerException | HttpStatusCodeException | ResourceAccessException e) {
                if(++fail==3){
                    baseAddress="http://localhost:0";
                    return false;
                }
                continue;
            }
            fail=0;
            baseAddress = baseAddress + roomId + "/devices/" + deviceId;
        }while (fail!=0);
        return true;
    }

    private String deviceID(GioPlantsResponse[] response){
        for(int i=0;i<response.length;i++){
            if(response[i].getMac().equals("FE:F4:1C:74:66:B3")){
                return response[i].getId();
            }
        }
        return null;

    }

    public HttpStatus watering() {
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress + "/actions/watering", HttpMethod.POST, HttpEntity.EMPTY, Void.class);
            status = response.getStatusCode();
        } catch (HttpStatusCodeException e) {
            status = e.getStatusCode();
        } catch (ResourceAccessException e) {
            if(connectToGioPlants()){
                status = watering();
            }else {
                status = HttpStatus.SERVICE_UNAVAILABLE;
            }
        }
        return status;
    }

    public ResponseEntity getTemperature(){
        ResponseEntity<GioPlantsResponse[]> response;
        try {
            response = restTemplate.getForEntity(baseAddress+"/readings?name=temperature&limit=5", GioPlantsResponse[].class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (ResourceAccessException e) {
            if(connectToGioPlants()){
                return getTemperature();
            }else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        GioPlantsResponse[] body = response.getBody();
        if(body==null || body.length==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(medianValue(body));

    }

    public ResponseEntity getMoisture(){
        ResponseEntity<GioPlantsResponse[]> response;
        try {
            response = restTemplate.getForEntity(baseAddress+"/readings?name=moisture&limit=5",GioPlantsResponse[].class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (ResourceAccessException e) {
            if(connectToGioPlants()){
                return getMoisture();
            }else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        GioPlantsResponse[] body = response.getBody();
        if(body==null || body.length==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(medianValue(body));

    }

    public ResponseEntity getBrightness(){
        ResponseEntity<GioPlantsResponse[]> response;
        try {
            response = restTemplate.getForEntity(baseAddress+"/readings?name=light&limit=5", GioPlantsResponse[].class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (ResourceAccessException e) {
            if(connectToGioPlants()){
                return getBrightness();
            }else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        GioPlantsResponse[] body = response.getBody();
        if(body==null || body.length==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(medianValue(body));
    }

   /* private HashMap<String,Float> avgValue(GioPlantsResponse[] body){
        float val=0.f;
        for(GioPlantsResponse g : body){
            val += Float.parseFloat(g.getValue());
        }
        val/=body.length;
        HashMap<String,Float> resp = new HashMap<>(1);
        resp.put("value",val);
        return resp;
    }*/

    private HashMap<String,Float> medianValue(GioPlantsResponse[] body){
        List<Float> values = Arrays.stream(body).map(r->Float.parseFloat(r.getValue())).sorted().collect(Collectors.toList());
        int middle = values.size()/2;
        float medianValue = 0.f;
        if (values.size()%2 == 1) {
            medianValue = values.get(middle);
        }else{
            medianValue = (values.get(middle-1) + values.get(middle) / 2);
        }
            HashMap<String,Float> resp = new HashMap<>(1);
        resp.put("value",medianValue);
        return resp;
    }

    public Float getMoistureValue(){
        Float moisture=null;
        try {
            ResponseEntity<GioPlantsResponse[]> response =  restTemplate.getForEntity(baseAddress+"/readings?name=moisture&limit=1",GioPlantsResponse[].class);
            GioPlantsResponse[] body = response.getBody();
            if(body!=null&& body.length!=0){moisture = Float.parseFloat(body[0].getValue());}
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return null;
        }
        return moisture;
    }


}
