package org.wso2.siddhi.loadbalancer.eventdivider;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.transport.TFileTransport;
import org.wso2.carbon.databridge.commons.Event;
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
            EventPublisher.publishEvents(nodelist.get(nodeCount).getHostname(), nodelist.get(nodeCount).getPort(), eventList);
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
        synchronized (eventList){
            while(true){
//        if(eventCount>0 && eventCount %1000==0){
//            EventPublisher.publishEvents(nodelist.get(nodeCount).getHostname(),nodelist.get(nodeCount).getPort(),eventList);
//            nodeCount++;
//            eventList.clear();
//        }
//        if(nodeCount== nodelist.size()){
//            nodeCount=1;
//        }


            }
        }

    }


}
