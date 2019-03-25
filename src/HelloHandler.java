import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class HelloHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.helloQueue.isEmpty()) {
                Packet p = Router.helloQueue.remove();
                String neighborID = p.srcAddress;
                int neighborPort = p.srcPort;
                String routerID = Router.routerID;
                Router.helloAck.put(neighborID, "11");
                Packet hello = new Packet();
                hello.type = 0;
                hello.srcAddress = routerID;
                hello.destPort = neighborPort;
                hello.destAddress = neighborID;
                hello.lsa = null;
                hello.cost = (int)System.currentTimeMillis();
                System.out.println("get hello from " + neighborID);
                try {
                    Socket helloSocket = new Socket(InetAddress.getByName(neighborID), UI.routerList.get(neighborID));
                    ObjectOutputStream out = new ObjectOutputStream(helloSocket.getOutputStream());
                    out.writeObject(hello);
                    System.out.println("reply hello to " + neighborID);
                    Router.helloAck.put(neighborID, "10");
                    int sendTime = (int) System.currentTimeMillis();
                    int count = 0;
                    while (Router.helloAck.get(neighborID).equals("10") && (int) (System.currentTimeMillis() - sendTime) > 30000 && count <= 2) {
                        count++;
                        Packet resend = new Packet();
                        resend.type = 0;
                        resend.srcAddress = routerID;
                        resend.destPort = neighborPort;
                        resend.destAddress = neighborID;
                        resend.lsa = null;
                        resend.cost = (int)System.currentTimeMillis();
                        out.writeObject(resend);
                        System.out.println("no reply resend");
                    }
                    helloSocket.close();
                    if(count == 2 && Router.helloAck.get(neighborID).equals("10")) {
                        System.out.println(neighborID + " is down!");
                        Router.helloAck.remove(neighborID);
                        Router.neighbors.remove(neighborID);
                        UI.neighbors.get(routerID).remove(neighborID);
                        Router.lsa.neighbors.remove(neighborID);
                        List<String> n = UI.neighbors.get(routerID);
                        for(int i = 0; i < n.size(); i++) {
                            //todo: need to change lsa handler when deal with incoming message in the queue, check if srcaddress is from the router itself and
                            //todo: process in different ways
                            String ngID = n.get(i);
                            int ngPort = UI.routerList.get(ngID);
                            Packet change = new Packet();
                            change.type = 1;
                            change.srcAddress = ngID;
                            change.destPort = UI.routerList.get(Router.routerID);
                            change.destAddress = Router.routerID;
                            change.lsa = Router.lsa;
                            Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
                            Router.lsaQueue.add(change);
                            System.out.println("router down, add change");
                        }

                    }
                } catch (IOException e) {
//                    System.out.println(neighborID + " is down!");
//                    Router.helloAck.remove(neighborID);
//                    Router.neighbors.remove(neighborID);
//                    UI.neighbors.get(Router.routerID).remove(neighborID);
//                    Router.lsa.neighbors.remove(neighborID);
//                    for(int j = 0; j < UI.neighbors.get(Router.routerID).size(); j++) {
//                        String ngID = UI.neighbors.get(Router.routerID).get(j);
//                        int ngPort = UI.routerList.get(ngID);
//                        Packet change = new Packet();
//                        change.type = 1;
//                        change.srcAddress = ngID;
//                        change.destPort = UI.routerList.get(Router.routerID);
//                        change.destAddress = Router.routerID;
//                        change.lsa = Router.lsa;
//                        Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
//                        Router.lsaQueue.add(change);
//                        System.out.println("router down, add change");
//                    }
                    e.printStackTrace();
                }

            }

            try {
                this.sleep(60000);
                broadcast();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

        public void broadcast() {
            List<String> n = UI.neighbors.get(Router.routerID);
            for(int i = 0; i < n.size(); i++) {
                String neighborID = n.get(i);
                int neighborPort = UI.routerList.get(neighborID);
                Packet hello = new Packet();
                hello.type = 0;
                hello.destAddress = neighborID;
                hello.destPort = neighborPort;
                hello.srcAddress = Router.routerID;
                hello.lsa = Router.lsa;
                hello.cost = (int)System.currentTimeMillis();
                try {
                    Socket socket = new Socket(InetAddress.getByName(neighborID), neighborPort);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(hello);
                    System.out.println("send hello to " + neighborID);
                    int count = 0;
                    int sendTime = (int)System.currentTimeMillis();
                    while (Router.helloAck.get(neighborID).equals("10") && (int) (System.currentTimeMillis() - sendTime) > 30000 && count <= 2) {
                        count++;
                        Packet resend = new Packet();
                        resend.type = 0;
                        resend.srcAddress = Router.routerID;
                        resend.destPort = neighborPort;
                        resend.destAddress = neighborID;
                        resend.lsa = null;
                        resend.cost = (int)System.currentTimeMillis();
                        out.writeObject(resend);
                        System.out.println("resend");
                    }
                    socket.close();
                    if(count == 2 && Router.helloAck.get(neighborID).equals("10")) {
                        System.out.println(neighborID + " is down!");
                        Router.helloAck.remove(neighborID);
                        Router.neighbors.remove(neighborID);
                        UI.neighbors.get(Router.routerID).remove(neighborID);
                        Router.lsa.neighbors.remove(neighborID);
                        for(int j = 0; j < n.size(); j++) {
                            String ngID = n.get(j);
                            int ngPort = UI.routerList.get(ngID);
                            Packet change = new Packet();
                            change.type = 1;
                            change.srcAddress = Router.routerID;
                            change.destPort = UI.routerList.get(ngID);
                            change.destAddress = ngID;
                            change.lsa = Router.lsa;
                            Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
                            Router.lsaQueue.add(change);
                            System.out.println("router down, add change");
                        }

                    }
                } catch (IOException e) {
//                        System.out.println(neighborID + " is down!");
//                        Router.helloAck.remove(neighborID);
//                        Router.neighbors.remove(neighborID);
//                        UI.neighbors.get(Router.routerID).remove(neighborID);
//                        Router.lsa.neighbors.remove(neighborID);
//                        for(int j = 0; i < n.size(); j++) {
//                            String ngID = n.get(j);
//                            int ngPort = UI.routerList.get(ngID);
//                            Packet change = new Packet();
//                            change.type = 1;
//                            change.srcAddress = ngID;
//                            change.destPort = UI.routerList.get(Router.routerID);
//                            change.destAddress = Router.routerID;
//                            change.lsa = Router.lsa;
//                            Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
//                            Router.lsaQueue.add(change);
//                            System.out.println("router down, add change");
//                        }
                    e.printStackTrace();
                }
            }
        }

}
