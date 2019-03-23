public class PingHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

    public void run() {
        while (isRunning()) {
            while (!Router.pingQueue.isEmpty()) {
                System.out.println("start to process ping");
                Packet p = Router.pingQueue.remove();
                String neighborID = p.srcAddress;
                int neighborPort = p.srcPort;
                System.out.println("neighbor port " + neighborPort);
                int cost = (int)(System.currentTimeMillis() - p.cost);
                if(Router.neighbors.get(neighborID) != cost) {
                    Router.neighbors.remove(neighborID);
                    Router.neighbors.put(neighborID, cost);
                    Router.lsa.neighbors = Router.neighbors;
                }
                System.out.println("finish process one ping");
            }

        }
    }
}
