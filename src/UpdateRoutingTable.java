import java.util.concurrent.ConcurrentHashMap;

public class UpdateRoutingTable extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            try{
                this.sleep(30000);
                Router.old_routingTable = Router.new_routingTable;
                Router.new_routingTable = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
                System.out.println("update routing table");
            } catch (InterruptedException e) {
                if(!isRunning()){
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
