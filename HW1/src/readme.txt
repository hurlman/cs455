There is no exit command for the Registry or MessagingNodes, so a CTRL+C is used.

Once the overlay is set up, it cannot be changed unless the program is exited and restarted.

No recovery from TCP disconnects, I did not run into any during testing unless doing CTRL+C.

The Messaging Node may deregister with the "exit-overlay" command, but this does not exit the program.
This is to show that a subsequent deregistration by that node will be detected as a fault by the
Registry.  There is no way to re-register, though, without closing (CTRL+C) and restarting the Messaging
Node.  Nodes can be de-registered and re-registered multiple times prior to the "setup-overlay" command
being run.



There are shortcuts for every keyboard input command as follows:

Shortcut    Full Command
n           list-messaging-nodes
o #         setup-overlay #     (# = number of routing table entries)
r           list-routnig-tables
s #         start #             (# = number of messages)

p           print-counters-and-diagnostics
d           exit-overlay


File List:
cs455/overlay/node/MessagingNode.java
    Main method and class for Messaging Nodes

cs455/overlay/node/Node.java
    Interface implemented by Registry and MessagingNodes.
    onEvent(Event, TCPConnection), onCommand(keyboard-command, arg)

cs455/overlay/node/Registry.java
    Main method and class for Registry

cs455/overlay/routing/Overlay.java
    Class containing data structures and methods for overlay setup and node tracking

cs455/overlay/routing/RoutingEntry.java
    Class representing node information and state

cs455/overlay/routing/RoutingTable.java
    Class containing data structure for node routing tables, methods for generating, relaying messages

cs455/overlay/transport/TCPConnection.java
    Class representing a socket connection, containing sender and receiver threads

cs455/overlay/transport/TCPReceiver.java
    Class reperesents TCP Listener thread.  Raises events when data is read from socket.

cs455/overlay/transport/TCPSender.java
    Class representing thread that sends data on socket.  Utilizes LinkedBlockingQueue.

cs455/overlay/transport/TCPConnectionCache.java
    Collection of TCP connections for a node

cs455/overlay/util/InteractiveCommandParser.java
    Keyboard input listener. Raises events to observer.  Enum defines all possible commands, shortcuts.

cs455/overlay/util/StatisticsCollectorAndDisplay.java
    Utility methods for tracking messages at a node, printing, as well as collating data at registry.

cs455/overlay/wireformats/Event.java
    Interface implemented by classes representing wire format messages.
    getType(), getBytes().

cs455/overlay/wireformats/EventFactory.java
    Singleton pattern, public method called by TCP Receivers to raise events to observers.

cs455/overlay/wireformats/Protocol.java
    MessageType enum, with method to get message type from int and vice versa.  Defines wait time.

Classes representing wire format messages.  Implement Event interface:
    cs455/overlay/wireformats/NodeReportsOverlaySetupStatus.java
    cs455/overlay/wireformats/OverlayNodeReportsTaskFinished.java
    cs455/overlay/wireformats/OverlayNodeReportsTrafficSummary.java
    cs455/overlay/wireformats/OverlayNodeSendsData.java
    cs455/overlay/wireformats/OverlayNodeSendsDeregistration.java
    cs455/overlay/wireformats/OverlayNodeSendsRegistration.java
    cs455/overlay/wireformats/RegistryReportsDeregistrationStatus.java
    cs455/overlay/wireformats/RegistryReportsRegistrationStatus.java
    cs455/overlay/wireformats/RegistryRequestsTaskInitiate.java
    cs455/overlay/wireformats/RegistryRequestsTrafficSummary.java
    cs455/overlay/wireformats/RegistrySendsNodeManifest.java