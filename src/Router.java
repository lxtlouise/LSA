
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Router {
    public static String routerID; //nitrogen.cs.pitt.edu
    int port; //listen port
    public static LSA lsa;
    public static boolean failure; //used to emulate router failure
    public static ConcurrentHashMap<String, LSA> LSDB;
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> ackTable; //<routerID, <neighborID, S/A>>
    public static ConcurrentHashMap<String, Integer> neighbors; //neighborID, cost
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> old_routingTable;
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> new_routingTable;
    public static ConcurrentHashMap<String, HelloNode> helloAck;

    public static List<Packet> receiveQueue; //need circular queue
    public static List<Packet> lsaQueue;
    public static List<Packet> helloQueue;
    public static List<Packet> pingQueue;
    public static List<Packet> ackQueue;
    public static List<Packet> lsaSendQueue; //for forwarding lsa
    public static List<Packet> requestQueue;
    public static List<Packet> helloAckQueue;
    public static List<Packet> fileQueue;

    ServerSocket serverSocket;
    public static ClientHandler clientHandler;
    public static LSAHandler lsaHandler;
    PingHandler pingHandler;
    public static HelloHandler helloHandler;
    AckHandler ackHandler;
    public static ServerThread serverThread;
    public static UpdateRoutingTable updateRoutingTable;
    public static LSASendHandler lsaSendHandler;
    EstablishHandler establishHandler;
    UpdateLSDB updateLSDB;
    public static CheckRouterAlive checkRouterAlive;
    public static HelloAckHandler helloAckHandler;
    public static FileHandler fileHandler;

    public Router(String routerID, int port, List<String> neighborsName) throws IOException {
        this.routerID = routerID;
        this.port = port;
        this.failure = false;
        LSDB = new ConcurrentHashMap<String, LSA>();
        ackTable = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
        neighbors = new ConcurrentHashMap<String, Integer>();
        for (int i = 0; i < neighborsName.size(); i++) {
            neighbors.put(neighborsName.get(i), Integer.MAX_VALUE);
        }
        lsa = new LSA(routerID, 30000, 0, neighbors);
        LSDB.put(routerID, lsa);

        helloAck = new ConcurrentHashMap<String, HelloNode>();
        for (int i = 0; i < neighborsName.size(); i++) {
            String neighborID = neighborsName.get(i);
            int count = 0;
            int time = (int) System.currentTimeMillis();
            String ack = "false";
            HelloNode hn = new HelloNode(neighborID, count, time, ack);
            helloAck.put(neighborID, hn);
        }

        old_routingTable = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>(); //<routerID, <neighborID, cost>>
        new_routingTable = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();

        receiveQueue = Collections.synchronizedList(new ArrayList<Packet>());
        lsaQueue = Collections.synchronizedList(new ArrayList<Packet>());
        helloQueue = Collections.synchronizedList(new ArrayList<Packet>());
        pingQueue = Collections.synchronizedList(new ArrayList<Packet>());
        ackQueue = Collections.synchronizedList(new ArrayList<Packet>());
        lsaSendQueue = Collections.synchronizedList(new ArrayList<Packet>());
        requestQueue = Collections.synchronizedList(new ArrayList<Packet>());
        helloAckQueue = Collections.synchronizedList(new ArrayList<Packet>());
        fileQueue = Collections.synchronizedList(new ArrayList<Packet>());

        serverSocket = new ServerSocket(port, 0, InetAddress.getByName(routerID));
        serverThread = new ServerThread(serverSocket, this);
        serverThread.start();

        clientHandler = new ClientHandler(this);
        clientHandler.start();

        lsaHandler = new LSAHandler();
        lsaHandler.start();

        pingHandler = new PingHandler();
        pingHandler.start();

        helloHandler = new HelloHandler();
        helloHandler.start();

        ackHandler = new AckHandler();
        ackHandler.start();

        updateRoutingTable = new UpdateRoutingTable();
        updateRoutingTable.start();

        lsaSendHandler = new LSASendHandler();
        lsaSendHandler.start();

        establishHandler = new EstablishHandler();
        establishHandler.start();

        updateLSDB = new UpdateLSDB();
        updateLSDB.start();

        checkRouterAlive = new CheckRouterAlive();
        checkRouterAlive.start();

        helloAckHandler = new HelloAckHandler();
        helloAckHandler.start();

        fileHandler = new FileHandler();
        fileHandler.start();
    }

}
