package org.wso2.siddhi.loadbalancer.eventdivider;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.transport.TFileTransport;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.siddhi.loadbalancer.eventpublisher.EventPublisher;
import org.wso2.siddhi.loadbalancer.nodemanager.Node;
import org.wso2.siddhi.loadbalancer.nodemanager.NodeProvider;


public class EventRRDivider implements Divider,Runnable {
    private static final List<Node> nodelist  = NodeProvider.getNodeListFromFile();
    private static int eventCount=0;
    private int nodeCount=0;
    private static List<Event> eventList = new ArrayList<Event>();


    @Override
    public synchronized void  divide(Event event) {
        eventCount++;
        eventList.add(event);
        if(eventCount >=10000){
            try {
                EventPublisher.publishEvents(nodelist.get(nodeCount).getHostname(), nodelist.get(nodeCount).getPort(), eventList);
            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MalformedStreamDefinitionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AgentException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (StreamDefinitionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            nodeCount++;
            eventList.clear();
            eventCount=0;
        }
        if(nodeCount== nodelist.size()){
            nodeCount=0;

        }
    }

    @Override
    public void run() {
        routeBufferedEvents();
    }

    //sending-event-count should be determined by considering the optimal packaging of events
    //i.e. : sending-event-count = max no: of events fit into a network packet
    //In this implementation test for exceeding the sending-event-count is performed
    //in the method bufferForRouting by setting sending-event-count to 10000.

    public  void bufferForRouting(List<Event> eventList) {
        synchronized(this.eventList){
                System.out.println("########### adding messages to the buffer ####################");
            this.eventList.addAll(eventList);
            //before notifying sender thread (i.e. the thread spawned using this class)
            //we are testing whether buffer exceed the sending-event-count
            if(!this.eventList.isEmpty() && this.eventList.size() >= 10000){
                this.eventList.notify();
                System.out.println("########### divider notified ####################");
            }
        }
    }

    public void routeBufferedEvents(){
        while(true) {
            List<Event> localEventList = new ArrayList<Event>();
            synchronized(eventList){
                System.out.println("########### divider started ####################");
                try {
                    System.out.println("########### In the try block before wait ####################");
                    eventList.wait();
                      System.out.println("########### In the try block after wait ####################");
                    //by adding a time interval as a parameter we can ensure thread will run periodically without a notify()
                    //by calling periodically we can ensure that every packet is sent without holding
                    //for the condition buffer doesn't exceed sending-event-count
                } catch (InterruptedException e) { }
                localEventList.addAll(eventList);
                eventList.clear();
            }

            //before sending events sender can check whether buffer has exceeded the sending-event-count
            //Or following code can be modified to send only a sending-event-count number of events
            try {
                EventPublisher.publishEvents(nodelist.get(nodeCount).getHostname(), nodelist.get(nodeCount).getPort(), localEventList);
            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MalformedStreamDefinitionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AgentException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (StreamDefinitionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            nodeCount++;
            localEventList.clear();

            //incrementing nodeCount to assure the round robin property
            if(nodeCount== nodelist.size()){
                nodeCount=0;

            }
        }
    }

}
