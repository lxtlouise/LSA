import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class AckHandler extends  Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }


    public void run() {
        while (isRunning()) {
            while(!Router.ackQueue.isEmpty()) {
                Packet p = Router.ackQueue.remove();
                System.out.println("get ack from " + p.type + " " + p.srcAddress + " " + p.srcPort);
                String neighborID = p.srcAddress;
                int neighborPort = p.srcPort;
                String routerID = p.destAddress;
                LSA newlsa = p.lsa;
                String lsaID = newlsa.routerID;
                Router.ackTable.get(lsaID).remove(neighborID);
                Router.ackTable.get(lsaID).put(neighborID, "11");
            }
        }
    }
}
