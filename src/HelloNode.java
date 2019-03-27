public class HelloNode {
    String neighborID;
    int counter;
    int sendTime;
    String ack;

    public HelloNode(String neighborID, int counter, int sendTime, String ack) {
        this.neighborID = neighborID;
        this.counter = counter;
        this.sendTime = sendTime;
        this.ack = ack;
    }
}
