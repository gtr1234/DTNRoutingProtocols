/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import util.Tuple;

import java.util.*;

public class MyRouter extends ActiveRouter {

    private Map<DTNHost, Integer> contacts;

    /**
     * Constructor. Creates a new message router based on the settings in
     * the given Settings object.
     * @param s The settings object
     */
    public MyRouter(Settings s) {
        super(s);
        this.contacts = new HashMap<>();
    }

    /**
     * Copy constructor.
     * @param r The router prototype where setting values are copied from
     */
    protected MyRouter(MyRouter r) {
        super(r);
        this.contacts = new HashMap<>();
    }

    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);

        if (con.isUp()) {
            DTNHost otherHost = con.getOtherNode(getHost());
            if(contacts.containsKey(otherHost)){
                int count = contacts.get(otherHost);
                contacts.put(otherHost, count+1);
            }
            else{
                contacts.put(otherHost, 1);
            }

        }
    }

    public int getNumberOfUniqueContacts() {
        return contacts.size();
    }


    @Override
    public void update() {
        super.update();
        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        // Try first the messages that can be delivered to final recipient
        if (exchangeDeliverableMessages() != null) {
            return; // started a transfer, don't try others (yet)
        }

        // then try any/all message to any/all connection
        tryOtherMessages();
    }

    private Tuple<Message, Connection> tryOtherMessages() {
        List<Tuple<Message, Connection>> messages = new ArrayList<>();

        Collection<Message> msgCollection = getMessageCollection();

        for (Connection con : getConnections()) {
            DTNHost other = con.getOtherNode(getHost());
            MyRouter othRouter = (MyRouter)other.getRouter();

            if (othRouter.isTransferring()) {
                continue; // skip hosts that are transferring
            }

            for (Message m : msgCollection) {
                if (othRouter.hasMessage(m.getId())) {
                    continue; // skip messages that the other one has
                }
                if (othRouter.getNumberOfUniqueContacts() > this.getNumberOfUniqueContacts()) {
                    messages.add(new Tuple<Message, Connection>(m,con));
                }
            }
        }

        if (messages.size() == 0) {
            return null;
        }

        // sort the message-connection tuples
        Collections.sort(messages, new MyRouter.TupleComparator());
        return tryMessagesForConnected(messages);	// try to send messages
    }

    private class TupleComparator implements Comparator
            <Tuple<Message, Connection>> {

        public int compare(Tuple<Message, Connection> tuple1,
                           Tuple<Message, Connection> tuple2) {
            int p1 = ((MyRouter)tuple1.getValue().
                    getOtherNode(getHost()).getRouter()).getNumberOfUniqueContacts();

            int p2 = ((MyRouter)tuple2.getValue().
                    getOtherNode(getHost()).getRouter()).getNumberOfUniqueContacts();

            if (p2-p1 == 0) {
                return compareByQueueMode(tuple1.getKey(), tuple2.getKey());
            }
            else if (p2-p1 < 0) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }


    @Override
    public MyRouter replicate() {
        return new MyRouter(this);
    }

}