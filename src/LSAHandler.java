import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
                Packet p = Router.lsaQueue.remove();
                LSA newlsa = p.lsa;
                String neighborID = p.srcAddress;
                int neighborPort = p.srcPort;
                String routerID = p.destAddress;
                String lsaID = newlsa.routerID; //routerID belongs to the lsa
                if (Router.LSDB.contains(lsaID)) {
                    LSA oldlsa = Router.LSDB.get(lsaID);
                    System.out.println("old lsa router id: " + oldlsa.routerID + " sequece " + oldlsa.sequence);
                    if(oldlsa.sequence >= newlsa.sequence) {
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
                    System.out.println("new lsa router id: " + acklsa.routerID + " sequece " + acklsa.sequence);
                }

                try { //send ack
                    System.out.println("neighbor: " + neighborID + " " + neighborPort);
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
                    System.out.println("ack lsa router id: " + acklsa.routerID + " sequece " + acklsa.sequence);
                    ackSockt.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (update == true) { //forward new lsa
                    List<String> n = UI.neighbors.get(routerID);
                    for (int i = 0; i < n.size(); i++) {
                        if(n.get(i).equals(neighborID)) {
                            continue;
                        } else {
                            String ngID = n.get(i);
                            int ngPort = UI.routerList.get(ngID);
                            try {
                                Socket lsaForward = new Socket(InetAddress.getByName(ngID), ngPort);
                                ObjectOutputStream lsaOut = new ObjectOutputStream(lsaForward.getOutputStream());
                                Packet lsaF = new Packet();
                                lsaF.type = 1;
                                lsaF.srcAddress = routerID;
                                lsaF.destAddress = ngID;
                                lsaF.destPort = ngPort;
                                lsaF.lsa = newlsa;
                                lsaF.cost = (int) System.currentTimeMillis();
                                lsaOut.writeObject(lsaF);
//                                while (true) {
//                                    try {
//                                        lsaForward.setSoTimeout(30000);
//                                        ObjectInputStream in = new ObjectInputStream(lsaForward.getInputStream());
//                                    } catch (SocketTimeoutException ex) {
//                                        lsaOut.writeObject(lsaF);
//                                        continue;
//                                    }
//                                }
                                if(Router.ackTable.contains(lsaID)) {
                                    Router.ackTable.get(lsaID).remove(ngID);
                                    Router.ackTable.get(lsaID).put(ngID, "10"); //update to send and wait for ack
                                } else {
                                    Router.ackTable.put((routerID), new ConcurrentHashMap<String, String>());
                                    Router.ackTable.get(lsaID).put(ngID, "10");
                                }
                                lsaForward.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}
