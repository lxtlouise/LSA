import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class EstablishHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.requestQueue.isEmpty()) {
                Packet p = Router.requestQueue.remove();
                String neighborID = p.srcAddress;
                String routerID = p.destAddress;
                if (p.count == 0) {
                    Packet response = new Packet();
                    response.type = 4;
                    response.srcAddress = routerID;
                    response.destPort = UI.routerList.get(neighborID);
                    response.destAddress = neighborID;
                    response.cost = (int) System.currentTimeMillis();
                    response.count = 1;
                    response.lsa = Router.lsa;
                    try {
                        Socket ackRequest = new Socket(InetAddress.getByName(neighborID), response.destPort);
                        ObjectOutputStream out = new ObjectOutputStream(ackRequest.getOutputStream());
                        out.writeObject(response);
                        ackRequest.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (p.count == 1) {
                    System.out.println("add link successfully");
                    int cost = (int) System.currentTimeMillis() - p.cost;
                    Router.lsa.neighbors.put(neighborID, cost);
                    Router.LSDB.put(routerID, Router.lsa);
                    Router.new_routingTable = new Routing().buildRoutingTable(Router.LSDB);
                    HelloNode hn = new HelloNode(neighborID, 0, (int)System.currentTimeMillis(), "false");
                    Router.helloAck.put(neighborID, hn);
                    Router.lsa.sequence++;
                    UI.neighbors.get(routerID).add(neighborID);
                    for (int i = 0; i < UI.neighbors.get(routerID).size() - 1; i++) {
                        String ng = UI.neighbors.get(routerID).get(i);
                        Packet lsaF = new Packet();
                        lsaF.type = 1;
                        lsaF.srcAddress = routerID;
                        lsaF.destAddress = ng;
                        lsaF.destPort = UI.routerList.get(ng);
                        lsaF.lsa = Router.lsa;
                        Router.lsaSendQueue.add(lsaF);
                    }
                    Packet confirm = new Packet();
                    confirm.type = 4;
                    confirm.srcAddress = routerID;
                    confirm.destPort = UI.routerList.get(neighborID);
                    confirm.destAddress = neighborID;
                    confirm.cost = (int) System.currentTimeMillis();
                    confirm.count = 2;
                    confirm.lsa = Router.lsa;
                    try {
                        Socket ackRequest = new Socket(InetAddress.getByName(neighborID), confirm.destPort);
                        ObjectOutputStream out = new ObjectOutputStream(ackRequest.getOutputStream());
                        out.writeObject(confirm);
                        ackRequest.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (p.count == 2) {
                    System.out.println("get add link confirm successfully");
                    int cost = (int) System.currentTimeMillis() - p.cost;
                    Router.lsa.neighbors.put(neighborID, cost);
                    Router.LSDB.put(routerID, Router.lsa);
                    Router.new_routingTable = new Routing().buildRoutingTable(Router.LSDB);
                    HelloNode hn = new HelloNode(neighborID, 0, (int)System.currentTimeMillis(), "false");
                    Router.helloAck.put(neighborID, hn);
                    Router.lsa.sequence++;
                    UI.neighbors.get(routerID).add(neighborID);
                    for (int i = 0; i < UI.neighbors.get(routerID).size() - 1; i++) {
                        String ng = UI.neighbors.get(routerID).get(i);
                        Packet lsaF = new Packet();
                        lsaF.type = 1;
                        lsaF.srcAddress = routerID;
                        lsaF.destAddress = ng;
                        lsaF.destPort = UI.routerList.get(ng);
                        lsaF.lsa = Router.lsa;
                        Router.lsaSendQueue.add(lsaF);
                    }
                }

            }
        }

    }

}
