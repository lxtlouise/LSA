import java.util.*;
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
//            System.out.println("build graph " + routerID);
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
    

    public ArrayList<Integer> Dijkstra(String s, String t) {
        int source = graph.getVertex(s);
        int target = graph.getVertex(t);
        ArrayList<Integer> path = new ArrayList<Integer>();
        if (graph == null) {
            System.out.println("Need to build graph first!");
        } else {
            PriorityQueue<Node> queue = new PriorityQueue<Node>(50, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    if (o1.weight > o2.weight) {
                        return 1;
                    } else if (o1.weight < o2.weight) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
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
                            Node c = new Node(n, weight);
                            c.prev = new ArrayList<>(temp.prev);
//                        		c.prev.add(temp.key);
                            c.prev.add(n);
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


    public HashMap<Integer, ArrayList<Integer>> dijkstra(String s) { //<destination, path>
        int source = graph.getVertex(s);
        HashMap<Integer, ArrayList<Integer>> result = new HashMap<>();
        for(Map.Entry<String, Integer> entry: UI.routerList.entrySet()) {
            String target = entry.getKey();
            if (target.equals(Router.routerID)) {
                continue;
            } else {
                int n = graph.getVertex(target);
                ArrayList<Integer> inner = Dijkstra(s, target);
                result.put(n, inner);
            }
        }

        return result;
    }
   
    
    
    class Node {
    		ArrayList<Integer> prev = new ArrayList<Integer>();
        int key; //neighbor vertex id
        int weight;
        Node(int key, int weight) {
            this.key = key;
            this.weight = weight;
        }

        public int compareTo(Node other){
            if(this.weight < other.weight) {
                return -1;
            } else if (this.weight > other.weight) {
                return 1;
            } else {
                return 0;
            }
//            return Integer.compare(this.weight, other.weight);
        }

    }

}
