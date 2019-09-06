package it.unipi.gio.vaseDriver.rest.in;

import it.unipi.gio.vaseDriver.model.GioPlantsResponse;
import it.unipi.gio.vaseDriver.rest.out.GioPlants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/sense")
public class SenseEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(SenseEndpoint.class);
    private GioPlants vase;

    @Autowired
    public SenseEndpoint(GioPlants vase){
        this.vase=vase;
    }

    @RequestMapping(value="/temperature",method = RequestMethod.GET)
    public ResponseEntity getTemperature() {
        LOG.info("Get temperature request");
        return vase.getTemperature();
    }

    @RequestMapping(value="/moisture",method = RequestMethod.GET)
    public ResponseEntity getMoisture() {
        LOG.info("Get moisture request");
        return vase.getMoisture();
    }

    @RequestMapping(value="/brightness",method = RequestMethod.GET)
    public ResponseEntity getBrightness() {
        LOG.info("Get brightness request");
        return vase.getBrightness();
    }
}
