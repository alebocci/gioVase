package it.unipi.gio.vaseDriver.rest.in;

import it.unipi.gio.vaseDriver.model.Goal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/*
* Class that manages the endpoint of the goals.
* Operations allowed: add new goal, get goal list, modify goal by id, delete goal by id*/
@RestController
@RequestMapping("/api/goal")
public class GoalEndpoint {

        /*Synchronized list faster to read than to write*/
        private CopyOnWriteArrayList<Goal> goalList;

        private static final Logger LOG = LoggerFactory.getLogger(GoalEndpoint.class);

        /*Unique id for every goal*/
        private AtomicInteger id = new AtomicInteger();

        @Autowired
        public GoalEndpoint(CopyOnWriteArrayList<Goal> goalList){
            this.goalList=goalList;
        }

        @RequestMapping(value = "/test", method = RequestMethod.GET)
        public ResponseEntity getTest() {
            LOG.info("Goal Test Request");
            return ResponseEntity.badRequest().body("" +
                    "{\n" +
                    "\t\"test\": \"ok\"\n" +
                    "}");
        }

        /*Return the list of goals in JSON, 204 No Content if the list is empty*/
        @RequestMapping(method = RequestMethod.GET)
        public ResponseEntity getGoals() {
            if(goalList.isEmpty()){return ResponseEntity.noContent().build();}
            return ResponseEntity.ok(goalList);
        }

        /**/
        @RequestMapping(method = RequestMethod.POST)
        public ResponseEntity setNewGoal(@RequestBody Goal g) {
            Float bottom = g.getMoistureBottom();
            Float top = g.getMoistureTop();
            if(bottom==null && top==null){
                return ResponseEntity.badRequest().body("You must set moisture property.");
            }

            int id = this.id.getAndIncrement();
            g.setId(id);
            goalList.add(g);
            return ResponseEntity.ok(g);
        }

        @RequestMapping(value="/{id}" , method = RequestMethod.PUT)
        public ResponseEntity modifyGoal(@RequestBody Goal g, @PathVariable("id") int id){
            if(id >= this.id.get() || id < 0){return ResponseEntity.notFound().build();}
            g.setId(id);
            id=findIndexById(id);
            goalList.set(id,g);
            return ResponseEntity.ok(g);
        }

        @RequestMapping(value="/{id}" , method = RequestMethod.DELETE)
        public ResponseEntity deleteGoal(@PathVariable("id") int id){
            if(id >= this.id.get() || id < 0){return ResponseEntity.notFound().build();}
            id=findIndexById(id);
            if(id==-1){return ResponseEntity.notFound().build();}
            goalList.remove(id);
            return ResponseEntity.ok().build();
        }

        private int findIndexById(int id){
            int i = id;
            if(i>=goalList.size()){
                i=goalList.size()-1;
            }
            Goal old = goalList.get(i);
            if(old.getId()!=id){
                while(i>=0){
                    old = goalList.get(i);
                    if(old.getId()==id){break;}
                    i--;
                }
            }
            return i;
        }

}
