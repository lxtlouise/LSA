import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
            ping.cost = (int) System.currentTimeMillis();
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
        System.out.println("(6) Stop router");
        System.out.println("(7) Recover router");
        System.out.println("(8) Transfer file");
        System.out.println("(9) Help");

        choice = Integer.parseInt(reader.nextLine());    // parse string input to a digit

        while (choice < 0 || choice > 7) { // checks for the correct input; if incorrect, loops until valid
            System.out.print("Please, enter a choice within menu options: ");
            choice = Integer.parseInt(reader.nextLine());
        }

        switch (choice) {
            case (0):
                System.exit(0);
            case (1):
                shortestPath();
                break;
            case (2):
                removeLink();
                break;
            case (3):
                addLink();
                break;
            case (4):
                routingTable();
                break;
            case (5):
                Ping(r);
                break;
            case (6):
                dropRouter();
                break;
            case (7):
                recoverRouter();
            case (8):
                transferFile();
            case (9):
                help();
                break;
        }
    }


    public static void routingTable() {
        try {
//            for (Map.Entry<String, ConcurrentHashMap<String, Integer>> entry : Router.new_routingTable.entrySet()) {
//                System.out.println(entry.getKey() + " " + entry.getValue());
//            }
            Routing routing = new Routing();
            WeightedGraph graph = routing.buildGraph(Router.old_routingTable);
            HashMap<Integer, ArrayList<Integer>> path = routing.dijkstra(Router.routerID);
            HashMap<String, String> result = new HashMap<>();
            for (Map.Entry<Integer, ArrayList<Integer>> entry : path.entrySet()) {
                int key = entry.getKey();
                ArrayList<Integer> temp = entry.getValue();
                int value = Integer.MAX_VALUE;
                if (temp != null) {
                     value = temp.get(temp.size() - 2);
                }
                result.put(graph.getLabel(key), graph.getLabel(value));

            }
            for (Map.Entry<String, String> entry: result.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void StartLSA(String routerID) throws IOException {
        List<String> n = neighbors.get(routerID);
        for (int i = 0; i < n.size(); i++) {
            Packet lsaMessage = new Packet();
            String neighborID = n.get(i);
            lsaMessage.type = 1;
            lsaMessage.srcAddress = routerID;
            lsaMessage.destAddress = neighborID;
            lsaMessage.destPort = UI.routerList.get(neighborID);
            lsaMessage.lsa = Router.lsa;
            lsaMessage.cost = (int) System.currentTimeMillis();
            Socket socket = new Socket(InetAddress.getByName(neighborID), lsaMessage.destPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(lsaMessage);
            if(Router.ackTable.contains(routerID)) {
                Router.ackTable.get(routerID).remove(routerID);
                Router.ackTable.get(routerID).put(routerID, "10"); //update to send and wait for ack
            } else {
                Router.ackTable.put((routerID), new ConcurrentHashMap<String, String>());
                Router.ackTable.get(routerID).put(routerID, "10");
            }
        }
        System.out.println("finish send lsa");
    }


    public static void shortestPath() {
        String source;
        String target;
        System.out.println("Source:");
        source = reader.nextLine().toLowerCase();
        System.out.println("Target:");
        target = reader.nextLine().toLowerCase();
        Routing routing = new Routing();
        WeightedGraph graph = routing.buildGraph(Router.old_routingTable);
        ArrayList<Integer> result = routing.Dijkstra(source, target);
        System.out.println(routing.getShortestPath(result));

    }

    public static synchronized void addLink() {
        System.out.println("Add link to: ");
        String neighborID = reader.nextLine().toLowerCase();
        Packet p = new Packet();
        p.destPort = UI.routerList.get(neighborID);
        p.destAddress = neighborID;
        p.srcAddress = Router.routerID;
        p.cost = (int) System.currentTimeMillis();
        p.lsa = Router.lsa;
        p.count = 0;
        p.type = 4;
        try {
            Socket socket = new Socket(InetAddress.getByName(neighborID), p.destPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(p);
            System.out.println("send add link request");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static synchronized void removeLink() {
        System.out.println("Drop link to: ");
        String router = reader.nextLine().toLowerCase();
        Router.lsa.neighbors.remove(router);
        UI.neighbors.remove(router);
        Router.LSDB.remove(router);
        for(int i = 0; i < Router.neighbors.size(); i++) {
            Packet p = new Packet();
            p.type = 1;
            Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
            p.lsa = Router.lsa;
            p.destAddress = UI.neighbors.get(router).get(i);
            p.srcAddress = Router.routerID;
            p.destPort = UI.routerList.get(p.destAddress);
            Router.lsaQueue.add(p);
            System.out.println("forward link failure");
        }
    }

    public static synchronized void dropRouter() {
        Router.serverThread.shutdown();
    }

    public static synchronized void recoverRouter() {
        Router.serverThread.start();
    }

    public static synchronized void transferFile() {
        System.out.println("Upload the file you need to transfer");
        String fileName = reader.nextLine();
        //uploadTranfer(fileName);
        System.out.println("Destination: ");
        String destination = reader.nextLine().toLowerCase();

    }
}
