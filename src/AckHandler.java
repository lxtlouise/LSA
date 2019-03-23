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

        }
    }
}
