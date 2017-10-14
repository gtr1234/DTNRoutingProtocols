package routing;/**
 * Created by Karthik on 13-Oct-17.
 */

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import util.Tuple;

import java.util.*;

public class NewRouter extends ActiveRouter{

    /**
     * Constructor. Creates a new message router based on the settings in
     * the given Settings object.
     * @param s The settings object
     */
    public NewRouter(Settings s) {
        super(s);
    }

    /**
     * Copy constructor.
     * @param r The router prototype where setting values are copied from
     */
    protected NewRouter(NewRouter r) {
        super(r);
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
            NewRouter othRouter = (NewRouter)other.getRouter();

            if (othRouter.isTransferring()) {
                continue; // skip hosts that are transferring
            }

            for (Message m : msgCollection) {
                if (othRouter.hasMessage(m.getId())) {
                    continue; // skip messages that the other one has
                }
                if (m.getHopsLeft() > 1) {

                    Message newMessage = new Message(m.getFrom(), m.getTo(), m.getId(), m.getSize());
                    newMessage.copyFrom(m);
                    newMessage.setHopsLeft(m.getHopsLeft() - 1);

                    messages.add(new Tuple<Message, Connection>(newMessage,con));
                }
            }
        }

        if (messages.size() == 0) {
            return null;
        }

        // sort the message-connection tuples
        Collections.sort(messages, new NewRouter.TupleComparator());
        return tryMessagesForConnected(messages);	// try to send messages
    }

    private class TupleComparator implements Comparator
            <Tuple<Message, Connection>> {

        public int compare(Tuple<Message, Connection> tuple1,
                           Tuple<Message, Connection> tuple2) {
            int p1 = tuple1.getKey().getHopsLeft();

            int p2 = tuple2.getKey().getHopsLeft();

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
    public NewRouter replicate() {
        return new NewRouter(this);
    }
}
