
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
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
    public static ConcurrentHashMap<String, String> helloAck;

    public static ConcurrentLinkedQueue<Packet> receiveQueue; //need circular queue
    public static ConcurrentLinkedQueue<Packet> lsaQueue;
    public static ConcurrentLinkedQueue<Packet> helloQueue;
    public static ConcurrentLinkedQueue<Packet> pingQueue;
    public static ConcurrentLinkedQueue<Packet> ackQueue;
    public static ConcurrentLinkedQueue<Packet> lsaSendQueue; //for forwarding lsa
    public static ConcurrentLinkedQueue<Packet> requestQueue;

    ServerSocket serverSocket;
    public static ClientHandler clientHandler;
    public static LSAHandler lsaHandler;
    PingHandler pingHandler;
    public static HelloHandler helloHandler;
    AckHandler ackHandler;
    public static ServerThread serverThread;
    UpdateRoutingTable updateRoutingTable;
    public static LSASendHandler lsaSendHandler;
    EstablishHandler establishHandler;
    UpdateLSDB updateLSDB;

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

        helloAck = new ConcurrentHashMap<String, String>();
        for (int i = 0; i < neighborsName.size(); i++) {
            helloAck.put(neighborsName.get(i), "00");
        }

        old_routingTable = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>(); //<routerID, <neighborID, cost>>
        new_routingTable = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();

        receiveQueue = new ConcurrentLinkedQueue<>();
        lsaQueue = new ConcurrentLinkedQueue<>();
        helloQueue = new ConcurrentLinkedQueue<>();
        pingQueue = new ConcurrentLinkedQueue<>();
        ackQueue = new ConcurrentLinkedQueue<>();
        lsaSendQueue = new ConcurrentLinkedQueue<>();
        requestQueue = new ConcurrentLinkedQueue<>();

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
    }

}
