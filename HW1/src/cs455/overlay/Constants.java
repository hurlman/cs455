/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay;

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 * @author Mike
 */
public class Constants {

    public enum MessageType {
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

        private int id;

        MessageType(int i) {
            id = i;
        }

        public byte GetByteValue() {
            return (byte) id;
        }

        public int GetIntValue() {
            return id;
        }

        public static MessageType GetMessageType(byte b) {
            int i = b;
            for (MessageType a : MessageType.values()) {
                if (a.GetIntValue() == i) {
                    return a;
                }
            }
            return null;
        }

        public static MessageType GetMessageType(int i) {
            for (MessageType a : MessageType.values()) {
                if (a.GetIntValue() == i) {
                    return a;
                }
            }
            return null;
        }

//        public static Constants.MessageType GetMessageType(byte b){
//            int i = b;
//            switch(i){
//                case 2:
//                    return OVERLAY_NODE_SENDS_REGISTRATION;
//                    case 3:
//                    return REGISTRY_REPORTS_REGISTRATION_STATUS;
//                    case 4:
//                    return OVERLAY_NODE_SENDS_DEREGISTRATION;
//                    case 5:
//                    return REGISTRY_REPORTS_DEREGISTRATION_STATUS;
//                    case 6:
//                    return REGISTRY_SENDS_NODE_MANIFEST;
//                    case 7:
//                    return NODE_REPORTS_OVERLAY_SETUP_STATUS;
//                    case 8:
//                    return REGISTRY_REQUESTS_TASK_INITIATE;
//                    case 9:
//                    return OVERLAY_NODE_SENDS_DATA;
//                    case 10:
//                    return OVERLAY_NODE_REPORTS_TASK_FINISHED;
//                    case 11:
//                    return REGISTRY_REQUESTS_TRAFFIC_SUMMARY;
//                    case 12:
//                    return OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY;
//            }
//            return null;
//        }
    }

    public static final int BUFFER_SIZE = 1024;

}
