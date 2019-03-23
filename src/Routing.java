import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Routing {
    WeightedGraph graph;
    ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> routingTable = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>(); //<routerID, <neighborID, cost>>

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> buildRoutingTable(ConcurrentHashMap<String, LSA> LSDB) {
        for (ConcurrentHashMap.Entry<String, LSA> entry : LSDB.entrySet()){
            String routerID = entry.getKey();
            LSA lsa = entry.getValue();
            routingTable.put(routerID, lsa.neighbors);
        }
        return routingTable;
    }

    public WeightedGraph buildGraph(ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> routingTable) {
        graph = new WeightedGraph(routingTable.size());
        int vertex = 0;
        for (ConcurrentHashMap.Entry<String, ConcurrentHashMap<String, Integer>> entry : routingTable.entrySet()){
            String routerID = entry.getKey();
            graph.setLabel(vertex, routerID);
            vertex++;
        }

        for (ConcurrentHashMap.Entry<String, ConcurrentHashMap<String, Integer>> entry : routingTable.entrySet()){
            String routerID = entry.getKey();
            ConcurrentHashMap<String, Integer> neighbors = entry.getValue();
            int source = graph.getVertex(routerID);
            for(ConcurrentHashMap.Entry<String, Integer> entry1 : neighbors.entrySet()) {
                String nb = entry1.getKey();
                int weight = entry1.getValue();
                int target = graph.getVertex(nb);
                graph.addEdge(source, target, weight);
            }
        }
        return graph;
    }
    

    public ArrayList<Integer> Dijkstra(int source, int target) {
        ArrayList<Integer> path = new ArrayList<Integer>();
        if (graph == null) {
            System.out.println("Need to build graph first!");
        } else {
            PriorityQueue<Node> queue = new PriorityQueue<Node>();
            boolean[] visited = new boolean[graph.getVertexNumber()];
            Node head = new Node(source, 0);
            head.prev.add(source);
            queue.add(head);     
            while(!queue.isEmpty()) {
                Node temp = queue.remove();
                visited[temp.key] = true;
                if(temp.key == target){
                		path = temp.prev;
                    break;
                } else {
                    ArrayList<Integer> neighbors = graph.getNeighbors(temp.key);
                    for(int i = 0; i < neighbors.size(); i++) {
                    		int n = neighbors.get(i);
                        if (visited[n] == false) {
                        		int weight = graph.getWeight(n, temp.key) + temp.weight;
                        		Node c = new Node(neighbors.get(i), weight);
                        		c.prev = new ArrayList<>(temp.prev);
//                        		c.prev.add(temp.key);
                        		c.prev.add(neighbors.get(i));
                             queue.add(c);
                        } else {
                        		continue;
                        }
                    }
                }
            }
        }
        
        return path;
    }

    public String getShortestPath(ArrayList<Integer> path) {
        String p = "";
        if (graph == null) {
            System.out.println("Need to build graph first!");
        } else {
            for(int i = 0; i < path.size(); i++) {
            		if (i == path.size()-1) {
            			int a = path.get(i);
            			String tt = graph.getLabel(path.get(i));
            			p = p + graph.getLabel(path.get(i));
            		} else {
            			p = p + graph.getLabel(path.get(i)) + " -> ";
            		}
            }
        }
        return p;
    }

   
    
    
    class Node implements Comparable<Node>{
    		ArrayList<Integer> prev = new ArrayList<Integer>();
        int key; //neighbor vertex id
        int weight;
        Node(int key, int weight) {
            this.key = key;
            this.weight = weight;
        }
        public int compareTo(Node other){
    			return Integer.compare(weight, other.weight);		
        }
    }
}
