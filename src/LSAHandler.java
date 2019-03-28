import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LSAHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.lsaQueue.isEmpty()) {
                boolean update = false;
                LSA acklsa = null;
                Packet p = Router.lsaQueue.remove(Router.lsaQueue.size() - 1);
                LSA newlsa = p.lsa;
                String neighborID = p.srcAddress;
                int neighborPort = p.srcPort;
                String routerID = Router.routerID;
                String lsaID = newlsa.routerID; //routerID belongs to the lsa
                if (Router.LSDB.contains(lsaID)) {
                    LSA oldlsa = Router.LSDB.get(lsaID);
//                    System.out.println("old lsa router id: " + oldlsa.routerID + " sequece " + oldlsa.sequence);
                    if (oldlsa.sequence >= newlsa.sequence) {
                        acklsa = oldlsa;
                        continue;
                    } else {
                        Router.LSDB.remove(lsaID);
                        Router.LSDB.put(lsaID, newlsa);
                        update = true;
                        acklsa = newlsa;
                    }
                } else {
                    acklsa = newlsa;
                    Router.LSDB.put(lsaID, newlsa);
                    update = true;
//                    System.out.println("new lsa router id: " + acklsa.routerID + " sequece " + acklsa.sequence);
                }

                try { //send ack
//                    System.out.println("neighbor: " + neighborID + " " + neighborPort);
                    Socket ackSockt = new Socket(InetAddress.getByName(neighborID), UI.routerList.get(neighborID));
                    ObjectOutputStream ackOut = new ObjectOutputStream(ackSockt.getOutputStream());
                    Packet ack = new Packet();
                    ack.type = 3;
                    ack.destPort = neighborPort;
                    ack.destAddress = neighborID;
                    ack.srcAddress = routerID;
                    ack.lsa = acklsa;
                    ack.cost = (int) System.currentTimeMillis();
                    ackOut.writeObject(ack);
//                    System.out.println("ack lsa router id: " + acklsa.routerID + " sequece " + acklsa.sequence);
                    ackSockt.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (update == true) { //forward new lsa
                    List<String> n = UI.neighbors.get(routerID);
                    for (int i = 0; i < n.size(); i++) {
                        if (n.get(i).equals(neighborID)) {
                            continue;
                        } else {
                            String ngID = n.get(i);
                            int ngPort = UI.routerList.get(ngID);
                                Packet lsaF = new Packet();
                                lsaF.type = 1;
                                lsaF.srcAddress = routerID;
                                lsaF.destAddress = ngID;
                                lsaF.destPort = ngPort;
                                lsaF.lsa = newlsa;
                                lsaF.cost = (int) System.currentTimeMillis();
                                Router.lsaSendQueue.add(lsaF);

                        }
                    }
                }
                    Router.new_routingTable = new Routing().buildRoutingTable(Router.LSDB);

            }

            try {
                this.sleep(30000);
            } catch (InterruptedException e) {
                if(!running){
                    break;
                }
            }
        }
    }


    public synchronized void shutdown(){
        this.running = false;
        interrupt();
    }

}
