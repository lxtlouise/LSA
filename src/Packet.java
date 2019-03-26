import java.io.Serializable;

public class Packet implements Serializable{
    int type;
    String srcAddress;
    int srcPort;
    String destAddress;
    int destPort;
    LSA lsa;
    int cost;
    int count;
}
