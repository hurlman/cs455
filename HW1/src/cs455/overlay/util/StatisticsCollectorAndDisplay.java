package cs455.overlay.util;

import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class used by both the Registry and MessagingNodes to collect and display information
 * about traffic sent, relayed, and received.
 */
public class StatisticsCollectorAndDisplay {

    private int sendTracker = 0;
    private int receiveTracker = 0;
    private int relayTracker = 0;
    private long sendSummation = 0;
    private long receiveSummation = 0;

    // Separate display fields that mirror trackers.  Required because trackers get reset on task complete.
    private int sentDisplay;
    private int receiveDisplay;
    private int relayDisplay;
    private long sendSumDisplay;
    private long receiveSumDisplay;

    private List<OverlayNodeReportsTrafficSummary> nodeTotals = new ArrayList<>();

    /**
     * Resets all counters after reporting totals to registry.
     */
    public void resetCounters() {
        sendTracker = 0;
        receiveTracker = 0;
        relayTracker = 0;
        sendSummation = 0;
        receiveSummation = 0;
    }

    /**
     * Incremenst sent tracker and adds to send sum.
     */
    public synchronized void dataSent(int payload) {
        sendTracker++;
        sendSummation += payload;
        sentDisplay = sendTracker;
        sendSumDisplay = sendSummation;
    }

    /**
     * Increments relay tracker.
     */
    public synchronized void dataRelayed() {
        relayTracker++;
        relayDisplay = relayTracker;
    }

    /**
     * Increments received tracker and receive sum.
     */
    public synchronized void dataReceived(int payload) {
        receiveTracker++;
        receiveSummation += payload;
        receiveDisplay = receiveTracker;
        receiveSumDisplay = receiveSummation;
    }

    public int getSendTracker() {
        return sendTracker;
    }

    public int getReceiveTracker() {
        return receiveTracker;
    }

    public int getRelayTracker() {
        return relayTracker;
    }

    public long getSendSummation() {
        return sendSummation;
    }

    public long getReceiveSummation() {
        return receiveSummation;
    }

    /**
     * Displays totals for this node.  Can during or after counters were reset.
     */
    public void NodeDisplay() {
        System.out.println();
        System.out.println("Totals for this round.");
        System.out.println("Sent: " + sentDisplay);
        System.out.println("Received: " + receiveDisplay);
        System.out.println("Relayed: " + relayDisplay);
        System.out.println("SentSum: " + sendSumDisplay);
        System.out.println("RecSum: " + receiveSumDisplay);
    }

    /**
     * Used by registry to store all node totals.
     */
    public synchronized void addNodeTotal(OverlayNodeReportsTrafficSummary nodeTotal) {
        nodeTotals.add(nodeTotal);
    }

    /**
     * Displays, sums, and displays sum for all nodes that have been collected.
     */
    public void printFinalReport() {
        sentDisplay = 0;
        receiveDisplay = 0;
        relayDisplay = 0;
        sendSumDisplay = 0;
        receiveSumDisplay = 0;

        //TODO Proper spacing so this doesn't look like trash.
        System.out.println("          Sent       Received     Relayed      Sum Sent           Sum Rec");
        nodeTotals.sort(Comparator.comparingInt(o -> o.NodeID));
        for (OverlayNodeReportsTrafficSummary o : nodeTotals) {
            System.out.println(String.format("Node %s   %s      %s      %s      %s          %s",
                    o.NodeID, o.Sent, o.Received, o.Relayed, o.SentSum, o.ReceivedSum));
            sentDisplay += o.Sent;
            receiveDisplay += o.Received;
            relayDisplay += o.Relayed;
            sendSumDisplay += o.SentSum;
            receiveSumDisplay += o.ReceivedSum;
        }
        System.out.println(String.format("Sum       %s     %s    %s      %s          %s",
                sentDisplay, receiveDisplay, relayDisplay, sendSumDisplay, receiveSumDisplay));

        nodeTotals.clear();
    }
}
