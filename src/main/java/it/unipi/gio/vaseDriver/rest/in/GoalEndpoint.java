package it.unipi.gio.vaseDriver.rest.in;

import it.unipi.gio.vaseDriver.GoalLogic;
import it.unipi.gio.vaseDriver.model.Goal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/*
* Class that manages the endpoint of the goal.
* Operations allowed: set new goal, get goal, delete goal, disable goal system*/
@RestController
@RequestMapping("/api/goal")
public class GoalEndpoint {

        private GoalLogic logic;

        private static final Logger LOG = LoggerFactory.getLogger(GoalEndpoint.class);

        @Autowired
        public GoalEndpoint(GoalLogic logic){
            this.logic=logic;
        }

        @RequestMapping(value = "/test", method = RequestMethod.GET)
        public ResponseEntity getTest() {
            LOG.info("Goal Test Request");
            return ResponseEntity.badRequest().body("" +
                    "{\n" +
                    "\t\"test\": \"ok\"\n" +
                    "}");
        }

        @RequestMapping(method = RequestMethod.GET)
        public ResponseEntity<Goal> getGoal() {
            Goal ret = logic.getGoal();
            if(ret==null){return ResponseEntity.notFound().build();}
            return ResponseEntity.ok(ret);
        }

        /**/
        @RequestMapping(method = RequestMethod.PUT)
        public ResponseEntity setNewGoal(@RequestBody Goal g) {
            Float bottom = g.getMoistureBottom();
            Float top = g.getMoistureTop();
            if(bottom==null && top==null){
                return ResponseEntity.badRequest().body("You must set moisture property.");
            }

             if(!logic.setGoal(g)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
             }
            return ResponseEntity.ok(g);
        }

        @RequestMapping(method = RequestMethod.DELETE)
        public ResponseEntity deleteGoal(){
            logic.setGoal(null);
            return ResponseEntity.ok().build();
        }

        @RequestMapping(value="/disable", method = RequestMethod.PUT)
        public ResponseEntity disableGoals(HttpServletRequest request,@RequestBody Map<String,String> body) {
            if(body==null || !body.containsKey("port")){
                return ResponseEntity.badRequest().body("Port where contact server not found");
            }
            String serverPort = body.get("port");
            LOG.info("Goal disable request at port "+serverPort);
            String url = "http://"+request.getRemoteAddr()+":"+serverPort;
            if(!logic.disactivateGoals(url)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok().build();
        }

        @RequestMapping(value="/enable", method = RequestMethod.PUT)
        public ResponseEntity enableGoals() {
            LOG.info("Enable goal request request");
            logic.activateGoals();
            return ResponseEntity.ok().build();
        }
}
