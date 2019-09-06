package it.unipi.gio.vaseDriver.rest.in;


import it.unipi.gio.vaseDriver.rest.out.GioPlants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/act")
public class ActEndpoint {


    private static final Logger LOG = LoggerFactory.getLogger(ActEndpoint.class);

    private GioPlants vase;

    @Autowired
    public ActEndpoint(GioPlants vase){
        this.vase=vase;
    }


    @RequestMapping(value="/vase/watering",method = RequestMethod.PUT)
    public ResponseEntity watering() {
        LOG.info("Watering request");
        HttpStatus response = vase.watering();
        return ResponseEntity.status(response).build();
    }

}
