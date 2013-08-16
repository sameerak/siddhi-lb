package org.wso2.siddhi.loadbalancer.eventdivider;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.siddhi.loadbalancer.nodemanager.Node;
import org.wso2.siddhi.loadbalancer.nodemanager.NodeProvider;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.query.Query;

import java.util.ArrayList;
import java.util.List;


public class EventStreamDivider implements Divider,Runnable {
    final static Query query = QueryFactory.createQuery();
    private static final List<Node> nodelist  = NodeProvider.getNodeListFromFile();
    private static List<Event> eventList = new ArrayList<Event>();




    @Override
    public synchronized void divide(Event event) {

            for(Node node:nodelist){
                if(node.getStreamID().equals(event.getStreamId())){
                    node.addEvent(event);
                    break;
                }

            }

    }

    @Override
    public void run() {
        assignEventsToNodes();
    }

    private void assignEventsToNodes() {
       while(true) {
            List<Event> localEventList = new ArrayList<Event>();
            synchronized(eventList){
                System.out.println("########### divider started ####################");
                try {
                    System.out.println("########### In the try block before wait ####################");
                    if (eventList.isEmpty())
                    eventList.wait();
                      System.out.println("########### In the try block after wait ####################");
                    //by adding a time interval as a parameter we can ensure thread will run periodically without a notify()
                    //by calling periodically we can ensure that every packet is sent without holding
                    //for the condition buffer doesn't exceed sending-event-count
                } catch (InterruptedException e) { }
                localEventList.addAll(eventList);
                eventList.clear();
            }

            for (Event evt : localEventList){
                for(Node node:nodelist){
                    if(evt.getStreamId().startsWith(node.getStreamID())){
                        node.addEvent(evt);
                        //this process should carry out for entire node list as there is a
                        //many-to-many relationship between stream IDs and nodes
                    }
                }
            }
            localEventList.clear();
        }
    }

    public  void bufferForRouting(List<Event> eventList) {
        synchronized(this.eventList){
            if(this.eventList.isEmpty()){  //notify sorter if event list is empty
                this.eventList.notify();
                System.out.println("########### divider notified ####################");
            }
            System.out.println("########### adding messages to the buffer ####################");
            this.eventList.addAll(eventList);
            //before notifying sender thread (i.e. the thread spawned using this class)
            //we are testing whether buffer exceed the sending-event-count
        }
    }
}
