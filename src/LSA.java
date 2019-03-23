import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class LSA implements Serializable{
    String routerID;
    double age;
    double sequence;
    ConcurrentHashMap<String, Integer> neighbors; //<neighborID, cost>

    public LSA(String routerID, double age, double sequence, ConcurrentHashMap<String, Integer> neighbors) {
        this.routerID = routerID;
        this.age = age;
        this.sequence = sequence;
        this.neighbors = neighbors;
    }

    public String getRouterID() {
        return routerID;
    }

    public void setRouterID(String routerID) {
        this.routerID = routerID;
    }

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public double getSequence() {
        return sequence;
    }

    public void setSequence(double sequence) {
        this.sequence = sequence;
    }

    public ConcurrentHashMap<String, Integer> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ConcurrentHashMap<String, Integer> neighbors) {
        this.neighbors = neighbors;
    }


}
