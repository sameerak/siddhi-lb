package org.wso2.siddhi.loadbalancer.nodemanager;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.siddhi.loadbalancer.eventpublisher.EventPublisher;

import java.util.ArrayList;
import java.util.List;


public class Node {

    private  String hostname;
    private  String port;
    private String streamID;
    private static List<Event> eventList = new ArrayList<Event>();
    private EventPublisher eventPublisher;
    private static Logger logger = Logger.getLogger(Node.class);

    public Node(String hostName, String port){
        this.hostname =hostName;
        this.port =port;
    }
    public Node(String hostName, String port,String streamID){
        this.hostname =hostName;
        this.port =port;
        this.streamID =streamID;
        try {
            eventPublisher = new EventPublisher(hostName, port);
        } catch (Exception e) {
            logger.info("Error when creating event publisher");
        }
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort(){
        return port;
    }
    public void setStreamID(String streamID){
        this.streamID = streamID;
    }
     public  String getStreamID(){
         return streamID;
     }

    public synchronized void addEvent(Event event){
        eventList.add(event);

        if (eventList.size()>=100){
            /*if(this.streamID != null){

            }*/
            try {
                EventPublisher.publishEvents(hostname,port,eventList);
            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MalformedStreamDefinitionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AgentException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (StreamDefinitionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            eventList.clear();
        }

    }



}
