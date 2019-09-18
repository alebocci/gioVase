package it.unipi.gio.vaseDriver.rest.in;

import it.unipi.gio.vaseDriver.rest.out.GioPlants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conf")
public class NetConfEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(NetConfEndpoint.class);

    private GioPlants vase;

    @Autowired
    public NetConfEndpoint(GioPlants vase){
        this.vase=vase;
    }



    @RequestMapping(value="/vasemac",method = RequestMethod.PUT)
    public ResponseEntity setVaseMac(@RequestBody String mac) {
        LOG.info("New vase mac request");
        vase.setMac(mac);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/vasemac",method = RequestMethod.GET)
    public ResponseEntity getVaseMac() {
        LOG.info("Get vase mac request");
        return ResponseEntity.ok(vase.getMac());
    }

    @RequestMapping(value={"/ip","/port","ipport"},method = RequestMethod.PUT)
    public ResponseEntity setIpPort(@RequestBody BodyIpPort body) {
        if(body==null){
            return ResponseEntity.badRequest().build();
        }
        LOG.info("New ip port request, ip: {} port: {}",body.ip,body.port);
        vase.setIpPort(body.ip,body.port);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/ip",method = RequestMethod.GET)
    public ResponseEntity getIp() {
        LOG.info("Get ip request");
        return ResponseEntity.ok(vase.getIp());
    }

    @RequestMapping(value="/port",method = RequestMethod.GET)
    public ResponseEntity getPort() {
        LOG.info("Get port request");
        return ResponseEntity.ok(vase.getPort());
    }

    public static class BodyIpPort{
        String ip;
        Integer port;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }
}
