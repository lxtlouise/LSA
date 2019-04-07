import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HelloHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }


    public void run() {
        while (isRunning()) {
            while(!Router.helloQueue.isEmpty()) {
                Packet p = Router.helloQueue.remove(Router.helloQueue.size() - 1);
                String neighborID = p.srcAddress;
                int neighborPort = UI.routerList.get(neighborID);
                Packet helloAck = new Packet();
                helloAck.type = 5;
                helloAck.destPort = neighborPort;
                helloAck.destAddress = neighborID;
                helloAck.srcAddress = Router.routerID;
                helloAck.cost = (int)System.currentTimeMillis();
                if(Router.neighbors.containsKey(neighborID)) {
                    try {
                        Socket socket = new Socket(InetAddress.getByName(neighborID), neighborPort);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(helloAck);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                System.out.println("get hello from: " + neighborID);
//                System.out.println("send hello ack to: " + neighborID);
                } else {
                    continue;
                }
            }


            try {
                this.sleep(50000);
                broadcast();
            } catch (InterruptedException e) {
                if(!running){
                    break;
                }
            }
        }

    }

    public void broadcast() {
        List<String> ng = UI.neighbors.get(Router.routerID);
        for (int i = 0; i < ng.size(); i++) {
            Packet hello = new Packet();
            String neighborID = ng.get(i);
            hello.type = 0;
            hello.srcAddress = Router.routerID;
            hello.destAddress = neighborID;
            hello.destPort = UI.routerList.get(neighborID);
            hello.cost = (int) System.currentTimeMillis();
            HelloNode hn = Router.helloAck.get(neighborID);
            try {
                if(InetAddress.getByName(neighborID).isReachable(50000)) {
                    if (!hn.ack.equals("pending")) {
                        hn.sendTime = hello.cost;
                        hn.counter = 0;
                        hn.ack = "pending";
                        Socket socket = new Socket(InetAddress.getByName(neighborID), hello.destPort);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(hello);
    //                    System.out.println("broadcast hello to: " + neighborID);
                        socket.close();
                    }
                }
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }
    


    public synchronized void shutdown(){
        this.running = false;
        interrupt();
    }


}
