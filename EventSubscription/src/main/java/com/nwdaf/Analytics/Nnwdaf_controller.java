package com.nwdaf.Analytics;


import com.nwdaf.Analytics.model.Namf_EventExposure.Namf_EventExposure_Subscribe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

import org.springframework.http.HttpHeaders;

import static java.lang.System.out;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpHeaders.USER_AGENT;


@RestController
public class Nnwdaf_controller {

    public static Logger logger = Logger.getLogger(Nnwdaf_controller.class.getName());

    Set<String> subID_SET = new HashSet<String>();

    String updated_POST_AMF_URL = null;

    private Log log = LogFactory.getLog(Nnwdaf_controller.class);

    public int subThValue = 0;
    public int currentLoadLevel = 0;

    List<UUID> subIDs;

    String msubID;

    public List<UUID> getSubIDs() {
        return subIDs;
    }

    public void setSubIDs(List<UUID> subIDs) {
        this.subIDs = subIDs;
    }

    public Nnwdaf_controller() {
        TestThread testThread = new TestThread();
        testThread.start();
    }


    public Nnwdaf_controller(List<UUID> subIDs) {
        this.subIDs = subIDs;
    }

    final String PATH = "/nnwdaf-eventssubscription/v1";

    @Value("${spring.MAIN.url}")
    String URI;

    @Value("${spring.NotificationTarget.Url}")
    String notificationTargetUrl;

    @Autowired
    Nnwdaf_repository repository;

    String unSubCorrelationID;


    @Value("${spring.AMF_EVENT_EXPOSURE_SUBSCRIBE.url}")
    String POST_AMF_URL;


    @GetMapping("apiroot/nnwdaf-analyticsinfo/v1/")
    public String testing() {


        return "HI Analytics";
    }


    @GetMapping("apiroot/nnwdaf-analyticsinfo/v1/{snssais}/{subscriptionID}")
    public ResponseEntity<?> loadLevelAllData(@PathVariable("snssais") String s, @PathVariable("subscriptionID") String sub) throws IOException, JSONException {

        List<events_connection> connect = repository.getData(s, sub);

        NnwdafEventsSubscription nnwdafEventsSubscription = new NnwdafEventsSubscription();
        nnwdafEventsSubscription.setSnssais(s);
        //  nnwdafEventsSubscription.setSubscriptionID(sub);

        if (connect.isEmpty()) {

            logger.warn("Data not found ");

            // calling collector function for Analytics
            //  UUID CorrelationID = collectorFuntion(nnwdafEventsSubscription);


            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
        } else {
            System.out.println(connect);
            return new ResponseEntity<events_connection>(HttpStatus.OK);
        }

    }


    @RequestMapping("/log")
    public String log() {
        log.trace("This is a TRACE level message");
        log.debug("This is a DEBUG level message");
        log.info("This is an INFO level message");
        log.warn("This is a WARN level message");
        log.error("This is an ERROR level message");

        //  Logger logger = LogManager.getLogger("CONSOLE_JSON_APPENDER");
        // logger.debug("Debug message");

        return "See the log for details";
    }


    @PostMapping(PATH + "/subscriptions")
    public ResponseEntity<String> subscribeNF(@RequestBody NnwdafEventsSubscription nnwdafEventsSubscription) throws SQLIntegrityConstraintViolationException, URISyntaxException, IOException, JSONException {



        UUID subID = UUID.randomUUID();
        logger.info("Subscription ID is generated - " + subID);

        //setting subID to nnwdafEventSubscription Object
        nnwdafEventsSubscription.setSubscriptionID(String.valueOf(subID));

        // adding data to nwdafSubscriptionTable;
        repository.subscribeNF(nnwdafEventsSubscription);

        //calling Collector Function
        UUID correlationID = collectorFuntion(nnwdafEventsSubscription);
        // logger.info("Collector Function called! ");

        //adding correlationID and unSubCorrelationID [ unSubCorrelationID will be updated later ]
       // repository.addCorrealationIDAndUnSubCorrelationIDIntoNwdafIDTable(correlationID,unSubCorrelationID);


        // working without correlation
        repository.addSubscriptionIdToLoadLevelInfo(nnwdafEventsSubscription, subID, correlationID);


        //  startThread();

        // TestThread testThread = new TestThread();
        // testThread.start();


        URI location = new URI(URI + "subscriptions/" + String.valueOf(subID));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);


        return new ResponseEntity<String>("Created", responseHeaders, HttpStatus.CREATED);
    }


    private UUID collectorFuntion(NnwdafEventsSubscription nnwdafEventsSubscription) throws IOException, JSONException {
        // logger.info("In Collector Function");

        UUID correlationID = UUID.randomUUID();


        Namf_EventExposure_Subscribe namf_eventExposure_subscribe = new Namf_EventExposure_Subscribe();


        // POST_AMF_URL = POST_AMF_URL + subId;
        //POST_AMF_URL = http::/localhost:8082/Namf_EventExposure_Subscribe/subID


        updated_POST_AMF_URL = POST_AMF_URL + "/" + correlationID;

        out.println("IN COLLECTOR FUNCITON " + updated_POST_AMF_URL);


        URL obj = new URL(updated_POST_AMF_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");

        JSONObject json = new JSONObject();

        // UUID correationId = UUID.randomUUID();
        //  Namf_EventExposure_Subscribe simulationDataObject = new Namf_EventExposure_Subscribe();


        //   simulationDataObject.setNfId(1234);

        // notificationTargetUrl = Namf_EventExposure_Notify
        // simulationDataObject.setNotificationTargetAddress(notificationTargetUrl);
        // simulationDataObject.setEventId(1);
        //    simulationDataObject.setCorrelationId(String.valueOf(correationID));


        //  json.put("subscriptionId", namf_eventExposure_subscribe.ge);
        json.put("correlationID", correlationID);
        json.put("unSubCorrelationId", namf_eventExposure_subscribe.getUnSubCorrelationId());

        // notificationTargetUrl = Namf_EventExposure_Notify
        json.put("notificationTargetAddress", notificationTargetUrl);
        //  System.out.println("\n");
        // logger.debug(" Data send by NWDAF to SIMULATOR :: " +
        //       " , nfID - " + simulationDataObject.getNfId() +
        //     " , notificationTargetAddress " + notificationTargetUrl +
        //    " , eventId " + simulationDataObject.getEventId() +
        //   " , correlationId " + simulationDataObject.getCorrelationId()
        //  );


        // For POST only - START
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = json.toString().getBytes("utf-8");

            //   System.out.println("\n");

            os.write(input, 0, input.length);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // For POST only - END

        int responseCode = con.getResponseCode();
        String responseMessage = con.getResponseMessage();
        System.out.println("\n");

        // logger.debug(" POST Response Code :: " + responseCode);

        // logger.info("Response received!");

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            // logger.debug("Collector subscription Worked!");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            // System.out.println("\n");
            //  logger.debug(" unSubCorrelationId [ Response from SIMULATOR ] :: " + response);

            logger.info("Response Received [unSubCorrelationID ]");

            out.println("\n\n\n\nUnsubCorrelationID "+ response.toString());
            out.println("\nCorrelationId - " +  correlationID);

           // repository.addCorrealationIDAndUnSubCorrelationIDIntoNwdafIDTable(correlationID,unSubCorrelationID);

            // save correlationID and unSubCorrelationID into nwdafIDTable /[ here is have to update unSubCorrelationID]
            repository.addCorrealationIDAndUnSubCorrelationIDIntoNwdafIDTable(correlationID,response.toString());

            logger.info("Stored response of collector into nwdafSubTable");


            //checking th values associated with subscription IDs
            //  checkThresholdValuesAndSendNotification(nnwdafEventsSubscription);


        } else {
            logger.warn(" POST request not worked");
        }

        return correlationID;

    }

    private List<NnwdafEventsSubscription> checkThresholdValuesAndSendNotification(NnwdafEventsSubscription nnwdafEventsSubscription) {

        EventID eventID = EventID.values()[nnwdafEventsSubscription.getEventID()];
        return repository.getALLSubID(eventID.ordinal());


    }


    @PutMapping(PATH + "/subscriptions/{subscriptionID}")
    public ResponseEntity<?> updateNF(@PathVariable("subscriptionID") String id, @RequestBody NnwdafEventsSubscription user) {
        NnwdafEventsSubscription check_user = repository.findById(id);

        if (check_user == null) {
            logger.warn("no Content");
            return new ResponseEntity<NnwdafEventsSubscription>(HttpStatus.NO_CONTENT);
        }

        repository.updateNF(user, id);

        return new ResponseEntity<NnwdafEventsSubscription>(HttpStatus.OK);
    }


    @DeleteMapping(value = PATH + "/subscriptions/{subscriptionID}")
    public ResponseEntity<?> unsubscribeNF(@PathVariable("subscriptionID") String id) {
        NnwdafEventsSubscription user = repository.findById(id);

        if (user == null) {
            return new ResponseEntity<NnwdafEventsSubscription>(HttpStatus.NOT_FOUND);
        }

        repository.unsubscribeNF(id);

        return new ResponseEntity<NnwdafEventsSubscription>(HttpStatus.NO_CONTENT);
    }


    @GetMapping(PATH + "/subscriptions/{subscriptionID}")
    public ResponseEntity<?> getNF(@PathVariable("subscriptionID") String id) {
        NnwdafEventsSubscription user = repository.findById(id);

        if (user == null) {
            return new ResponseEntity<NnwdafEventsSubscription>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<NnwdafEventsSubscription>(user, HttpStatus.OK);
    }


    // Accepting Notification
    @RequestMapping(method = RequestMethod.POST, value = "/Namf_EventExposure_Notify/{subscriptionID}")
    public void acceptingNotification(@PathVariable String subscriptionID) {
        logger.debug(" Data Received for : " + subscriptionID);
        System.out.println("Data received !!");


    }


    @RequestMapping("/Namf_EventExposure_UnSubscribe/{unSubCorrelationId}")
    public String unSubscribeEvent(@PathVariable UUID unSubCorrelationId) {

        //  collectorRepository.unSubscribeEventViaUnSubCorrelationId(unSubCorrelationId);
        return " Event Deleted";
    }


    public void startThread() {
        logger.info("In Start Thread function");
        //   TestThread testThread = new TestThread();
        // testThread.start();
        logger.info("Thread started.");


    }


    public List<UUID> getAllSubIds(List<NnwdafEventsSubscription> nnwdafEventsSubscription) {

        logger.info("fetching all subIDs");

        List<UUID> allSubIds = new ArrayList<>();


        for (int i = 0; i < nnwdafEventsSubscription.size(); i++) {

            allSubIds.add(nnwdafEventsSubscription.get(i).subscriptionID);
        }

        return allSubIds;

    }


    public class TestThread extends Thread {

        public void run() {


            while (true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("In Thread!");

                callNotificationManagerFunction();

            }
        }

        private void callNotificationManagerFunction() {

            List<UUID> ids = getAllSubIds(repository.getALLSubID(0));  //eventID.ordinal();

            logger.debug(ids.size());

            for (int i = 0; i < ids.size(); i++) {

                logger.debug(ids.get(i) + "iteration");
                System.out.println("Total subscribers [ I am in Thread ]" + ids.size());

                //  subThValue = repository.findById(String.valueOf(ids.get(i))).getLoadLevelThreshold();

                if (repository.findById(String.valueOf(ids.get(i))) == null) {
                    logger.warn("Null object");
                } else {
                    subThValue = repository.findById(String.valueOf(ids.get(i))).getLoadLevelThreshold();
                }

                logger.debug("subThValue" + subThValue);

                // check -- after commenting this line it gives null pointer exception;;
                // [TODO]
                // need review;;!
                // logger.debug(" \n Current Load Level  in Load_Level_info Table [ " +
                //       repository.findDataByuSubId(String.valueOf(ids.get(i))).getSubscriptionID() + " ] "
                //     + " Available Load  " + repository.findDataByuSubId(String.valueOf(ids.get(i))).getLoadLevelThreshold());


                if (repository.findDataByuSubId(String.valueOf(ids.get(i))) == null) {
                    logger.warn("Null Object Value!");
                } else {
                    currentLoadLevel = repository.findDataByuSubId(String.valueOf(ids.get(i))).getLoadLevelThreshold();
                }
                //  currentLoadLevel = repository.findDataByuSubId(String.valueOf(ids.get(i))).getLoadLevelThreshold();

                logger.debug("current Load Level " + currentLoadLevel);

                if (currentLoadLevel > subThValue) {

                    logger.debug("current Load level" + currentLoadLevel + "sub Th value" + subThValue);


                    logger.debug(subID_SET.size());

                    if (repository.findDataByuSubId(String.valueOf(ids.get(i))) == null) {
                        logger.warn("Null Object!");
                    } else {

                        msubID = repository.findDataByuSubId(String.valueOf(ids.get(i))).getSubscriptionID();
                    }



                    logger.debug(msubID);

                    if (!(subID_SET.contains(msubID))) {

                        logger.debug(subID_SET.size());


                        subID_SET.add(msubID);

                        logger.debug(subID_SET.size());
                        logger.debug(subID_SET.iterator().next());

                        try {

                            //  sendNotificationToNF(repository.findById(repository.findDataByuSubId(String.valueOf(ids.get(i))).getSubscriptionID()).getNotificationURI());     // URI from NIKHIL Table;
                          if (repository.findById(msubID) == null){
                              logger.warn("Null Object Found!");
                          }else{
                              sendNotificationToNF(repository.findById(msubID).getNotificationURI());
                          }
                        //   sendNotificationToNF(repository.findById(msubID).getNotificationURI());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }

        private void sendNotificationToNF(String notificationURI) throws IOException {


            out.println(notificationURI);

            URL url = null;
            try {
                url = new URL(notificationURI);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // Opening connection;
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                con.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            // JSON  String sending to M1
            // String jsonInputString = "{\"id\":10000,\"name\":\" [ M2 -> M1 ]\"}";

            String jsonInputString = "Hey I am Notification";

            System.out.println("\n\nSending Notification From  NWDAF TO NF  - " + jsonInputString);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");

                os.write(input, 0, input.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Read the response from input stream;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                out.println("Response code from NF - " + HttpStatus.valueOf(con.getResponseCode()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
