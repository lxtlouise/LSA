import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class UpdateLSDB extends Thread {
    protected boolean running = true;

    public synchronized void shutdown(){
        this.running = false;
    }

    public synchronized void restart() {
        this.running = true;
    }

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            for(Map.Entry<String, LSA> entry : Router.LSDB.entrySet()) {
                String lsaID = entry.getKey();
                LSA lsa = entry.getValue();
                if (lsa.age == 0) {
                    Router.LSDB.remove(lsaID);
                    System.out.println("update LSDB: " + lsa.routerID);
                }
            }
        }

        try {
            this.sleep(10000);
            for(Map.Entry<String, LSA> entry : Router.LSDB.entrySet()) {
                String lsaID = entry.getKey();
                LSA lsa = entry.getValue();
                lsa.age--;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
