import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UI {
    public static Scanner reader = new Scanner(System.in);
    public static ConcurrentHashMap<String, Integer> routerList; //<routerID, port>
    public static ConcurrentHashMap<String, List<String>> neighbors; //<routerID, neighbors>
//    public static ConcurrentHashMap<String, Router> routers; //<routerID, Router class>

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        System.out.println("Please upload configuration files to start");
        String fileName = reader.nextLine();
        uploadFile(fileName);
        System.out.println("Please choose which router to start");
        String r = reader.nextLine();
        start(r);


        while (true) {
            menu(r);
        }


    }

    public static void uploadFile(String file) throws InterruptedException, IOException, ClassNotFoundException {
        routerList = new ConcurrentHashMap<String, Integer>();
        neighbors = new ConcurrentHashMap<String, List<String>>();
//        routers = new ConcurrentHashMap<String, Router>();
        String line = "";
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            line = fileReader.readLine();
            String routerID = "";
            int port = 0;
            List<String> ng = Collections.synchronizedList(new ArrayList<String>());
            boolean flag = false;
            while (line != null) {
                if (line.startsWith("router")) {
                    String[] temp = line.split(":");
                    routerID = temp[1];
                } else if (line.startsWith("port")) {
                    String[] temp = line.split(":");
                    port = (int) Double.parseDouble(temp[1]);
                } else if (line.startsWith("neighbors")) {
                    String[] temp = line.split(":");
                    for (int i = 0; i < temp.length; i++) {
                        if (i == 0) {
                            continue;
                        } else {
                            ng.add(temp[i]);
                        }
                    }
                    flag = true;
                }
                if (flag == true) {
                    routerList.put(routerID, port);
                    neighbors.put(routerID, ng);
                    flag = false;
                    routerID = " ";
                    port = 0;
                    ng = new ArrayList<String>();
                }
                line = fileReader.readLine();
            }

            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void help() {
        System.out.println("(a) Select (1) to show the shortest path between source and target. Enter server names for both source and target.");
        System.out.println("(b) Select (2) to remove link. Enter server names for both source and neighbor ID.");
        System.out.println("(c) Select (3) to add link. Enter server names for both source and neighbor ID.");
        System.out.println("(d) Select (4) to display routing table for particular router. Enter server names for router.");
        System.out.println("(e) Select (5) to ping neighbors. ");
    }


    public static void start(String routerID) throws InterruptedException, IOException, ClassNotFoundException {
        int port = routerList.get(routerID); //enter the router that we want to start
        List<String> n = neighbors.get(routerID);
        Router router = new Router(routerID, port, n); //only start one router
//        routers.put(routerID, router);
    }

    public static void Ping(String routerID) throws IOException, ClassNotFoundException {
        List<String> n = neighbors.get(routerID);
        for (int i = 0; i < n.size(); i++) {
            Packet ping = new Packet();
            String neighborID = n.get(i);
            ping.type = 2;
            ping.srcAddress = routerID;
            ping.destAddress = neighborID;
            ping.destPort = UI.routerList.get(neighborID);
            ping.lsa = Router.lsa;
            Socket socket = new Socket(InetAddress.getByName(neighborID), ping.destPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(ping);
        }
        System.out.println("finish ping");
    }

    public static void menu(String r) throws IOException, ClassNotFoundException {
        int choice;

        System.out.println("Please select an option:");
        System.out.println("(1) Display shortest path");
        System.out.println("(2) Remove a link");
        System.out.println("(3) Add a link");
        System.out.println("(4) Display routing table");
        System.out.println("(5) Ping");
        System.out.println("(6) Help");

        choice = Integer.parseInt(reader.nextLine());    // parse string input to a digit

        while (choice < 0 || choice > 6) { // checks for the correct input; if incorrect, loops until valid
            System.out.print("Please, enter a choice within menu options: ");
            choice = Integer.parseInt(reader.nextLine());
        }

        switch (choice) {
            case (0):
                System.exit(0);
            case (1):
//                shortestPath();
                break;
            case (2):
//                removeLink();
                break;
            case (3):
//                addLink();
                break;
            case (4):
                routingTable();
                break;
            case (5):
                Ping(r);
                break;
            case (6):
                help();
                break;
        }
    }


    public static void routingTable() {
        try {
            for (Map.Entry<String, ConcurrentHashMap<String, Integer>> entry : Router.new_routingTable.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
