import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{
    ServerSocket serverSocket;
    Router router;
    protected boolean running = true;

    public ServerThread(ServerSocket serverSocket, Router router) {
        this.serverSocket = serverSocket;
        this.router = router;
    }

    public void run(){
        while (isRunning()) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                System.out.println(router  + "server socket accept " + clientSocket.getPort());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                Packet p = (Packet)in.readObject();
                p.srcPort = clientSocket.getPort();
                Router.receiveQueue.add(p);
                System.out.println("recieve queue size: " + clientSocket.getPort() + " " + router.receiveQueue.size());
            } catch (IOException e){
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }

    }

    private synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized void shutdown(){
        this.running = false;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
}
