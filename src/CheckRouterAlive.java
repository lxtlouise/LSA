import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

public class CheckRouterAlive extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            try {
                this.sleep(30000);
                checkAlive();
            } catch (InterruptedException e) {
                if (!isRunning()) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkAlive() throws IOException {
//        System.out.println("check aliveness");
        int timeout = 50000;
        for(Map.Entry<String, HelloNode> entry : Router.helloAck.entrySet()){
            String neighborID = entry.getKey();
            HelloNode hn = entry.getValue();
            int time = (int)System.currentTimeMillis() - hn.sendTime;
//            System.out.println(hn.neighborID + " " + hn.ack + " " + hn.counter + " " + time);
            Socket socket = null;
            if (hn.counter < 3 && hn.ack.equals("pending") && time > timeout) {
//                System.out.println("time lag is " + time);
                Packet resend = new Packet();
                resend.type = 0;
                resend.srcAddress = Router.routerID;
                resend.destAddress = neighborID;
                resend.destPort = UI.routerList.get(neighborID);
                resend.cost = (int) System.currentTimeMillis();
                hn.sendTime = resend.cost;
                hn.counter++;
                hn.ack = "pending";
                socket = new Socket(InetAddress.getByName(neighborID), resend.destPort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(resend);
//                System.out.println("resend hello " + hn.counter);
            }
            if (hn.counter >= 3 && hn.ack.equals("pending")) {
                System.out.println ("neighbor is down " + neighborID);
                Router.helloAck.remove(neighborID);
                Router.neighbors.remove(neighborID);
                UI.neighbors.get(Router.routerID).remove(neighborID);
                Router.lsa.neighbors.remove(neighborID);
                Router.LSDB.put(Router.routerID, Router.lsa);
                Router.new_routingTable = new Routing().buildRoutingTable(Router.LSDB);
                for(int j = 0; j < UI.neighbors.get(Router.routerID).size(); j++) {
                    String ngID = UI.neighbors.get(Router.routerID).get(j);
                    int ngPort = UI.routerList.get(ngID);
                    Packet change = new Packet();
                    change.type = 1;
                    change.srcAddress = Router.routerID;
                    change.destPort = UI.routerList.get(ngID);
                    change.destAddress = ngID;
                    change.lsa = Router.lsa;
                    change.lsa.sequence++;
                    Router.lsaSendQueue.add(0, change);
                    System.out.println("router down, broadcast change using lsa");
                }
            }
            if (socket != null) {
                socket.close();
            }
        }
    }

    public synchronized void shutdown(){
        this.running = false;
        interrupt();
    }

}
