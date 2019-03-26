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
                Packet p = Router.lsaQueue.remove();
                LSA newlsa = p.lsa;
                String neighborID = p.srcAddress;
                int neighborPort = p.srcPort;
                String routerID = Router.routerID;
                String lsaID = newlsa.routerID; //routerID belongs to the lsa
                if (Router.LSDB.contains(lsaID)) {
                    LSA oldlsa = Router.LSDB.get(lsaID);
                    System.out.println("old lsa router id: " + oldlsa.routerID + " sequece " + oldlsa.sequence);
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
                        if (n.get(i).equals(neighborID)) {
                            continue;
                        } else {
                            String ngID = n.get(i);
                            int ngPort = UI.routerList.get(ngID);
//                            try {
//                                Socket lsaForward = new Socket(InetAddress.getByName(ngID), ngPort);
//                                ObjectOutputStream lsaOut = new ObjectOutputStream(lsaForward.getOutputStream());
                                Packet lsaF = new Packet();
                                lsaF.type = 1;
                                lsaF.srcAddress = routerID;
                                lsaF.destAddress = ngID;
                                lsaF.destPort = ngPort;
                                lsaF.lsa = newlsa;
                                lsaF.cost = (int) System.currentTimeMillis();
//                                lsaOut.writeObject(lsaF);
                                Router.lsaSendQueue.add(lsaF);
                                int sendTime = (int) System.currentTimeMillis();
//                                if (Router.ackTable.contains(lsaID)) {
//                                    Router.ackTable.get(lsaID).remove(ngID);
//                                    Router.ackTable.get(lsaID).put(ngID, "10"); //update to send and wait for ack
//                                } else {
//                                    Router.ackTable.put((routerID), new ConcurrentHashMap<String, String>());
//                                    Router.ackTable.get(lsaID).put(ngID, "10");
//                                }
//                                while (Router.ackTable.get(Router.routerID).get(ngID).equals("10") && (int) (System.currentTimeMillis() - sendTime) > 100000) {
//                                    Packet resend = new Packet();
//                                    resend.type = 1;
//                                    resend.srcAddress = routerID;
//                                    resend.destAddress = ngID;
//                                    resend.destPort = ngPort;
//                                    resend.lsa = newlsa;
//                                    resend.cost = (int) System.currentTimeMillis();
//                                    lsaOut.writeObject(resend);
//                                }
//                                lsaForward.close();
//                            }
//                            catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                }
                    Router.new_routingTable = new Routing().buildRoutingTable(Router.LSDB);

            }

            try {
                this.sleep(30000);
                broadcast();
                Router.new_routingTable = new Routing().buildRoutingTable(Router.LSDB);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
//                try {
//                    Socket lsaForward = new Socket(InetAddress.getByName(ngID), ngPort);
//                    ObjectOutputStream lsaOut = new ObjectOutputStream(lsaForward.getOutputStream());
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
//                    lsaOut.writeObject(lsaF);
                    if(InetAddress.getByName(lsaF.srcAddress).isReachable(50000)) {
                        Router.lsaSendQueue.add(lsaF);
                        System.out.println("broadcast lsa");
                    }
                    int sendTime = (int) System.currentTimeMillis();
//                    if(Router.ackTable.contains(Router.routerID)) {
//                        Router.ackTable.get(Router.routerID).remove(ngID);
//                        Router.ackTable.get(Router.routerID).put(ngID, "10"); //update to send and wait for ack
//                    } else {
//                        Router.ackTable.put((Router.routerID), new ConcurrentHashMap<String, String>());
//                        Router.ackTable.get(Router.routerID).put(ngID, "10");
//                    }
//                    while(Router.ackTable.get(Router.routerID).get(ngID).equals("10") && (int) (System.currentTimeMillis() - sendTime) > 100000){
//                        Packet resend = new Packet();
//                        resend.type = 1;
//                        resend.srcAddress = Router.routerID;
//                        resend.destAddress = ngID;
//                        resend.destPort = ngPort;
//                        resend.lsa = newlsa;
//                        resend.cost = (int) System.currentTimeMillis();
//                        lsaOut.writeObject(resend);
//                    }
//                    lsaForward.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
    }


}
