import java.util.ArrayList;

public class WeightedGraph {
    int[][] graph;
    String[] labels; //map ip address to each vertex(integer) in the graph

    public WeightedGraph(int n) {
        graph = new int[n][n]; //initialize graph
        labels = new String[n];
    }

    public int getVertexNumber(){
        return graph[0].length;
    }

    public void setLabel(int vertex, String label){ //associate each vertex with ip address
        labels[vertex] = label;
    }

    public String getLabel(int vertex){
        return labels[vertex];
    }

    public int getVertex(String label) {
        for(int i = 0; i < labels.length; i++) {
            if (labels[i].equals(label)){
                return i;
            }
        }
        System.out.println("Error: no such IP address!");
        return -1;
    }

    public void addEdge(int source, int target, int weight) {
        graph[source][target] = weight;
    }

    public boolean removeEdge(int source, int target) {
        graph[source][target] = Integer.MAX_VALUE;
        return true;
    }

    public ArrayList<Integer> getNeighbors(int vertex) {
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        int[] temp = graph[vertex];
        for(int i = 0; i < temp.length; i++) {
            if (graph[vertex][i] != 0) {
                neighbors.add(i);
            } else {
                continue;
            }
        }
        return neighbors;
    }
    

    public int getWeight(int source, int target) {
        return graph[source][target];
    }
}
