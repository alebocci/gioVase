package it.unipi.gio.vaseDriver.rest.out;

import it.unipi.gio.vaseDriver.model.GioPlantsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GioPlants {

    private static final Logger LOG = LoggerFactory.getLogger(GioPlants.class);


    private InetAddress ip;
    private int port;
    private String baseAddress;
    private RestTemplate restTemplate;
    private String mac = "FE:F4:1C:74:66:B3";

    public GioPlants(InetAddress ip, int port, RestTemplate restTemplate, String vaseMac){
        if(ip==null) return;
        this.ip = ip;
        this.port = port;
        this.restTemplate = restTemplate;
        this.mac=vaseMac;
        connectToGioPlants();

    }

    private boolean connectToGioPlants(){
        baseAddress = "http://"+ip.getHostName()+":"+port+"/rooms/";
        int fail = 0;
        do {
            String roomId ;
            String deviceId ;
            try {
                ResponseEntity<GioPlantsResponse[]> response = restTemplate.getForEntity(baseAddress, GioPlantsResponse[].class);
                if (response == null || response.getBody() == null || response.getBody().length == 0 || response.getStatusCode().isError()) {
                    if(++fail==3){
                        baseAddress="http://localhost:0";
                        return false;
                    }
                    continue;
                }
                roomId =response.getBody()[0].getId();
                response = restTemplate.getForEntity(baseAddress + roomId + "/devices", GioPlantsResponse[].class);
                if (response == null || response.getBody() == null || response.getBody().length == 0 || response.getStatusCode().isError()) {
                    if(++fail==3){
                        baseAddress="http://localhost:0";
                        return false;
                    }
                    continue;
                }
                deviceId = deviceID(response.getBody());
                if (deviceId == null) return false;
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
        for (GioPlantsResponse gioPlantsResponse : response) {
            if (gioPlantsResponse.getMac().equals(mac)) {
                return gioPlantsResponse.getId();
            }
        }
        return null;

    }

    public HttpStatus watering() {
        HttpStatus status;
        try {
            String request = "{\"value\":"+5+"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Void> response = restTemplate.exchange(baseAddress + "/actions/watering", HttpMethod.POST, entity, Void.class);
            status = response.getStatusCode();
            if(status.isError()){
                if(connectToGioPlants()){
                    int errors=0;
                    do {
                        response = restTemplate.exchange(baseAddress + "/actions/watering", HttpMethod.POST, entity, Void.class);
                        status = response.getStatusCode();
                        if(!status.isError()){
                            break;
                        }
                        errors++;
                    }while (errors<3);
                    if(errors==3){
                        status = HttpStatus.SERVICE_UNAVAILABLE;
                    }
                }else {
                    status = HttpStatus.SERVICE_UNAVAILABLE;
                }
            }
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
        ResponseEntity<GioPlantsResponse[]> response=null;
        try {
            response = restTemplate.getForEntity(baseAddress+"/readings?name=temperature&limit=1", GioPlantsResponse[].class);

        }catch (HttpStatusCodeException e){
            if(e.getStatusCode().isError()){
                if(connectToGioPlants()){
                    int errors=0;
                    do {
                        response = restTemplate.getForEntity(baseAddress+"/readings?name=temperature&limit=1", GioPlantsResponse[].class);
                        if(!response.getStatusCode().isError()){
                            break;
                        }
                        errors++;
                    }while (errors<3);
                    if(errors==3){
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                    }
                }else {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                }
            }
        } catch (ResourceAccessException e) {
            if(connectToGioPlants()){
                return getTemperature();
            }else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        if(response==null){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        GioPlantsResponse[] body = response.getBody();
        if(body==null || body.length==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(medianValue(body));

    }

    public ResponseEntity getMoisture(){
        ResponseEntity<GioPlantsResponse[]> response=null;
        try {
            response = restTemplate.getForEntity(baseAddress+"/readings?name=moisture&limit=10",GioPlantsResponse[].class);

        }catch (HttpStatusCodeException e){
            if(e.getStatusCode().isError()){
                if(connectToGioPlants()){
                    int errors=0;
                    do {
                        response = restTemplate.getForEntity(baseAddress+"/readings?name=moisture&limit=10",GioPlantsResponse[].class);
                        if(!response.getStatusCode().isError()){
                            break;
                        }
                        errors++;
                    }while (errors<3);
                    if(errors==3){
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                    }
                }else {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                }
            }
        } catch (ResourceAccessException e) {
            if(connectToGioPlants()){
                return getMoisture();
            }else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        if(response==null){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        GioPlantsResponse[] body = response.getBody();
        if(body==null || body.length==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(maxValue(body));

    }

    public ResponseEntity getBrightness(){
        ResponseEntity<GioPlantsResponse[]> response=null;
        try {
            response = restTemplate.getForEntity(baseAddress+"/readings?name=light&limit=5", GioPlantsResponse[].class);

        }catch (HttpStatusCodeException e){
            if(e.getStatusCode().isError()){
                if(connectToGioPlants()){
                    int errors=0;
                    do {
                        response = restTemplate.getForEntity(baseAddress+"/readings?name=light&limit=5", GioPlantsResponse[].class);
                        if(!response.getStatusCode().isError()){
                            break;
                        }
                        errors++;
                    }while (errors<3);
                    if(errors==3){
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                    }
                }else {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                }
            }
        } catch (ResourceAccessException e) {
            if(connectToGioPlants()){
                return getBrightness();
            }else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        if(response==null){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
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
        float medianValue;
        if (values.size()%2 == 1) {
            medianValue = values.get(middle);
        }else{
            medianValue = (values.get(middle-1) + values.get(middle))/ 2;
        }
            HashMap<String,Float> resp = new HashMap<>(1);
        resp.put("value",medianValue);
        return resp;
    }

    private HashMap<String,Float> maxValue(GioPlantsResponse[] body){
        float max = Arrays.stream(body).map(r->Float.parseFloat(r.getValue())).max(Float::compareTo).orElse(0f);
        max = max * 100 / 255; //percentage
        HashMap<String,Float> resp = new HashMap<>(1);
        resp.put("value",max);
        return resp;
    }

    public Float getMoistureValue(){
        ResponseEntity<GioPlantsResponse[]> response=null;
        try {
            response =  restTemplate.getForEntity(baseAddress+"/readings?name=moisture&limit=10",GioPlantsResponse[].class);
            GioPlantsResponse[] body = response.getBody();
            if(body==null || body.length==0){
                return null;
            }
        }catch (HttpStatusCodeException e){
            if(e.getStatusCode().isError()){
                if(connectToGioPlants()){
                    int errors=0;
                    do {
                        response =  restTemplate.getForEntity(baseAddress+"/readings?name=moisture&limit=10",GioPlantsResponse[].class);
                        if(!response.getStatusCode().isError()){
                            break;
                        }
                        errors++;
                    }while (errors<3);
                    if(errors==3){
                        return null;
                    }
                }else {
                    return null;
                }
            }
        }catch (ResourceAccessException e){
            return null;
        }
        if(response==null){
            return null;
        }
        return maxValue(response.getBody()).get("value");
    }

    public synchronized String getMac() {
        return mac;
    }

    public synchronized void setMac(String mac) {
        this.mac = mac;
    }

    public synchronized InetAddress getIp() {
        return ip;
    }



    public synchronized int getPort() {
        return port;
    }

    private boolean setIp(String ip) {
        if(ip==null){
            return false;
        }
        if(!this.ip.getHostName().equals(ip)){
            try {
                this.ip = InetAddress.getByName(ip);
                return true;
            } catch (UnknownHostException | IllegalArgumentException e) {
                //do nothing
            }
        }
        return false;
    }

    private boolean setPort(Integer port) {
        if(port==null){
            return false;
        }
        if(port!=this.port){
            this.port=port;
            return true;
        }
       return false;
    }

    public synchronized void setIpPort(String ip, Integer port){
        if(setIp(ip) || setPort(port)) {
            connectToGioPlants();
        }
    }
}
