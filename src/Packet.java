import java.io.Serializable;

public class Packet implements Serializable{
    int type;
    String srcAddress;
    int srcPort;
    String destAddress;
    int destPort;
    LSA lsa;
    int cost;
    int count; //for establish connection
    byte[] file; //used for transferring file, need to set type as 6 and attach file
    String[] path;
    int pathIndex;
    String fileName;
}
