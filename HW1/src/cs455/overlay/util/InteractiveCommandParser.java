package cs455.overlay.util;


import cs455.overlay.node.Node;

public class InteractiveCommandParser extends Thread {
    public enum Command {
        LIST_MESSAGING_NODES("n", "list-messaging-nodes"),
        SETUP_OVERLAY("o", "setup-overlay"),
        LIST_ROUTING_TABLES("r","list-routing-tables"),
        START("s","start"),
        PRINT_COUNTERS_AND_DIAGNOSTICS("p","print-counters-and-diagnostics"),
        EXIT_OVERLAY("d","exit-overlay"),
        UNKNOWN("","");

        private final String longCommand;
        private final String shortCommand;

        Command(String s, String l) {
            shortCommand = s;
            longCommand = l;
        }

        public static Command getCommand(String input){
            for(Command a : Command.values()){
                if(input.equals(a.longCommand) || input.equals(a.shortCommand)){
                    return a;
                }
            }
            return UNKNOWN;
        }
    }

    private Node Subscriber;

    public InteractiveCommandParser(Node subscriber){
        Subscriber = subscriber;
    }

    public void run(){
        while(true){
            String[] inputs = System.console().readLine().split("\\s+");
            int arg = -1;
            if (inputs.length > 1){
                try{
                    arg = Integer.parseInt(inputs[1]);
                }
                catch (NumberFormatException e){
                    System.out.println(String.format("%s is not a valid integer", inputs[1]));
                    continue;
                }
            }
            Command cmd = Command.getCommand(inputs[0]);
            switch(cmd){
                case LIST_MESSAGING_NODES:
                    Subscriber.onCommand(cmd, -1);
                    break;
                case SETUP_OVERLAY:
                    Subscriber.onCommand(cmd, arg);
                    break;
                case LIST_ROUTING_TABLES:
                    Subscriber.onCommand(cmd, -1);
                    break;
                case START:
                    Subscriber.onCommand(cmd, arg);
                    break;
                case PRINT_COUNTERS_AND_DIAGNOSTICS:
                    Subscriber.onCommand(cmd, -1);
                    break;
                case EXIT_OVERLAY:
                    Subscriber.onCommand(cmd, -1);
                    break;
                case UNKNOWN:
                    System.out.println("Unknown command.");
                    break;
            }
        }
    }

}
