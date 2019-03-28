import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LSASendHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.lsaSendQueue.isEmpty()) {
                Packet p = Router.lsaSendQueue.remove(Router.lsaSendQueue.size() - 1);
                String neighborID = p.destAddress;
                int neighborPort = p.destPort;
                String lsaID = p.lsa.routerID;
                String routerID = p.srcAddress;
                if(Router.lsa.neighbors.containsKey(neighborID)) {
                    try {
                        Socket socket = new Socket(InetAddress.getByName(neighborID), neighborPort);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(p);
//                        System.out.println("forward lsa");
                        if (Router.ackTable.contains(lsaID)) {
                            Router.ackTable.get(lsaID).remove(neighborID);
                            Router.ackTable.get(lsaID).put(neighborID, "10"); //update to send and wait for ack
                        } else {
                            Router.ackTable.put((lsaID), new ConcurrentHashMap<String, String>());
                            Router.ackTable.get(lsaID).put(neighborID, "10");
                        }
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Router.ackTable.get(lsaID).get(neighborID).equals("10") && (int) (System.currentTimeMillis() - p.cost) > 100000) {
                        Packet resend = new Packet();
                        resend.cost = (int) System.currentTimeMillis();
                        Router.lsaSendQueue.add(resend);
//                        System.out.println("resend lsa");
                    }
                } else {
                    continue;
                }
            }

            try {
                this.sleep(30000);
                broadcast();
                Router.new_routingTable = new Routing().buildRoutingTable(Router.LSDB);
            } catch (InterruptedException e) {
                if(!running){
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void broadcast() throws IOException {
        List<String> n = UI.neighbors.get(Router.routerID);
        for (int i = 0; i < n.size(); i++) {
            String ngID = n.get(i);
            int ngPort = UI.routerList.get(ngID);
            LSA newlsa = Router.lsa;
            newlsa.sequence++;
            Router.LSDB.put(Router.routerID, newlsa);
            Packet lsaF = new Packet();
            lsaF.type = 1;
            lsaF.srcAddress = Router.routerID;
            lsaF.destAddress = ngID;
            lsaF.destPort = ngPort;
            lsaF.lsa = newlsa;
            lsaF.cost = (int) System.currentTimeMillis();
            if(InetAddress.getByName(lsaF.srcAddress).isReachable(50000)) {
                Router.lsaSendQueue.add(lsaF);
//                System.out.println("broadcast lsa");
            }
            if(Router.ackTable.containsKey(Router.routerID)) {
                ConcurrentHashMap<String, String> inner = Router.ackTable.get(Router.routerID);
                inner.put(ngID, "10");
                Router.ackTable.put(Router.routerID, inner);
            } else {
                ConcurrentHashMap<String, String> inner = new ConcurrentHashMap<>();
                inner. put(ngID, "10");
                Router.ackTable.put(Router.routerID, inner);
            }

        }
    }


    public synchronized void shutdown(){
        this.running = false;
        interrupt();
    }


}
