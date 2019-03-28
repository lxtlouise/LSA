import java.net.Socket;

public class ClientHandler extends Thread{
    protected boolean running = true;
    Socket client = null;
    Router router; //router of server

    public static boolean flag = false;

    public ClientHandler(Router router) {
        this.router = router;
    }

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.receiveQueue.isEmpty()) {
                Packet message = Router.receiveQueue.remove(Router.receiveQueue.size() - 1);
                System.out.println("client handler remove one message from receive queue " + message.type + " " + message.srcAddress);
                if (message.type == 0) { //hello
                    Router.helloQueue.add(message);
                } else if (message.type == 1) { //lsa
                    Router.lsaQueue.add(message);
                } else if (message.type == 2) { //ping
                    Router.pingQueue.add(message);
                } else if (message.type == 3) { //ack
                    Router.ackQueue.add(message);
                } else if (message.type == 4) { //connect request
                    Router.requestQueue.add(message);
                } else if (message.type == 5) { //hello ack
                    Router.helloAckQueue.add(message);
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
