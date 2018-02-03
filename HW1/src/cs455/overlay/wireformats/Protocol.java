/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;

import cs455.overlay.*;

/**
 * @author Mike
 */
public class Protocol {

    public final static int RELAY_WAIT = 3;

    public enum MessageType {

        UNKNOWN(0),
        OVERLAY_NODE_SENDS_REGISTRATION(2),
        REGISTRY_REPORTS_REGISTRATION_STATUS(3),
        OVERLAY_NODE_SENDS_DEREGISTRATION(4),
        REGISTRY_REPORTS_DEREGISTRATION_STATUS(5),
        REGISTRY_SENDS_NODE_MANIFEST(6),
        NODE_REPORTS_OVERLAY_SETUP_STATUS(7),
        REGISTRY_REQUESTS_TASK_INITIATE(8),
        OVERLAY_NODE_SENDS_DATA(9),
        OVERLAY_NODE_REPORTS_TASK_FINISHED(10),
        REGISTRY_REQUESTS_TRAFFIC_SUMMARY(11),
        OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY(12);

        private final int id;

        MessageType(int i) {
            id = i;
        }

        public int GetIntValue() {
            return id;
        }

        public static MessageType getMessageType(int i) {
            for (MessageType a : MessageType.values()) {
                if (a.GetIntValue() == i) {
                    return a;
                }
            }
            return UNKNOWN;
        }

    }
}
