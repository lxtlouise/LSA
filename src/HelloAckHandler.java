public class HelloAckHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.helloAckQueue.isEmpty()) {
                Packet helloAck = Router.helloAckQueue.remove();
                String neighborID = helloAck.srcAddress;
                int neighborPort = UI.routerList.get(neighborID);
                HelloNode hn = Router.helloAck.get(neighborID);
                hn.ack = "true";
                hn.counter = 0;
                System.out.println("get hello ack from: " + neighborID);
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
