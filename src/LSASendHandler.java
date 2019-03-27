import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class LSASendHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.lsaSendQueue.isEmpty()) {
                Packet p = Router.lsaSendQueue.remove();
                String neighborID = p.destAddress;
                int neighborPort = p.destPort;
                String lsaID = p.lsa.routerID;
                String routerID = p.srcAddress;
                if(Router.lsa.neighbors.containsKey(neighborID)) {
                    try {
                        Socket socket = new Socket(InetAddress.getByName(neighborID), neighborPort);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(p);
                        System.out.println("forward lsa");
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
                    while (Router.ackTable.get(lsaID).get(neighborID).equals("10") && (int) (System.currentTimeMillis() - p.cost) > 100000) {
                        Packet resend = new Packet();
                        resend.cost = (int) System.currentTimeMillis();
                        Router.lsaSendQueue.add(resend);
                        System.out.println("resend lsa");
                    }
                } else {
                    continue;
                }
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

    public synchronized void restart() {
        this.running = true;
    }

}
