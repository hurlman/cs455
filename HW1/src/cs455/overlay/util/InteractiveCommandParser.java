package cs455.overlay.util;


import cs455.overlay.node.Node;

import java.util.Scanner;

public class InteractiveCommandParser extends Thread {
    public enum Command {
        LIST_MESSAGING_NODES("l", "list-messaging-nodes"),
        SETUP_OVERLAY("s", "setup-overlay"),
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
        Scanner keyboard = new Scanner(System.in);
        while(true){
            String[] inputs = keyboard.nextLine().split(" ");
            int arg = -1;
            if (inputs.length > 1){
                try{
                    arg = Integer.parseInt(inputs[1]);
                }
                catch (NumberFormatException e){
                    System.out.println("Invalid command. " + e.getMessage());
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
                case UNKNOWN:
                    System.out.println("Unknown command.");
                    break;
            }
        }
    }

}
