import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;

public class FileHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.fileQueue.isEmpty()) {
                Packet p = Router.fileQueue.remove(Router.fileQueue.size() - 1);
                String neighborID = p.srcAddress;
                int neighborPort = UI.routerList.get(neighborID);
                String[] path = p.path;
                int pathIndex = p.pathIndex;
                byte[] data = p.file;
                if(path[path.length - 1].equals(Router.routerID)) {
                    System.out.println("file gets to the destinaton");
                    try {
                        Files.write(new File(p.fileName).toPath(), data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
//                    System.out.println("send file to next hop");
                    Packet file = p;
                    file.pathIndex = pathIndex++;
                    file.srcAddress = Router.routerID;
                    file.destAddress = path[pathIndex];
                    file.destPort = UI.routerList.get(file.destAddress);
                    file.type = 6;
                    Socket socket = null;
                    try {
                        socket = new Socket(InetAddress.getByName(file.destAddress), file.destPort);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(file);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
}
