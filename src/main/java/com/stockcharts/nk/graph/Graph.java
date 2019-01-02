package com.stockcharts.nk.graph;
import java.util.*;
import org.json.JSONObject;

public class Graph<T> {

    private final Map<String, Node<T>> nodes;
    private final Map<String, Edge> edges;

    public Graph() {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
    }

    public Graph(Map<String, Node<T>> nodes, Map<String, Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }
    
    public Graph(List<T> data, EdgeCalculator<T> calculator) {
        
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
        
        int i = 0;
        for (T d : data) {
            Node node = new Node(String.valueOf(i++), d);
            addNode(node);
        }
        
        List<Edge> edges = calculator.calculateEdges(nodes.values());        
        for (Edge edge : edges) {
            addEdge(edge);
        }
        
    }
    
    public Map<String, Node<T>> getNodes() {
        return nodes;
    }

    public Map<String, Edge> getEdges() {
        return edges;
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }
    
    public void addEdge(Edge edge) {
        if (nodes == null) System.out.println("NULL");
        edges.put(edge.getId(), edge);
        nodes.get(edge.getSource()).addEdge(edge.getId());
        nodes.get(edge.getTarget()).addEdge(edge.getId());
    }
    
    
    public static Graph<Person> generateRandomPersonGraph(int numNodes, int maxConnections) {
    
        List<Person> people = new LinkedList<>();
        
        for (int i = 0; i < numNodes; i++) {
            Person person = new Person(String.valueOf(i));
            people.add(person);
        }
        
        for (int i = 0; i < people.size(); i++) {
            
            Person person = people.get(i);
            
            for (int r = 0; r < (int)(Math.random() * maxConnections); r++) {

                int randomConnection;
                do {
                    randomConnection = (int) (Math.random() * (numNodes - 1));
                } while (randomConnection == i && person.getConnections().contains(String.valueOf(randomConnection)) == false);

               person.addConnection(String.valueOf(randomConnection));
               people.get(randomConnection).addConnection(String.valueOf(i));
           }
        }
                        
        return new Graph<>(people, new PersonEdgeCalculator());
    }
    
    public static Graph generateRandomGraph(int numNodes, int numEdges, int maxEdgeValue) {
        
        Graph graph = new Graph();
        
        for (int i = 0; i < numNodes; i++) {
            Node<String> node = new Node<>("n" + i, "Node " + i);
            graph.addNode(node);
        }
        
        for (int i = 0; i < numEdges; i++) {
            
            String source = "n" + (int) (Math.random() * graph.getNodes().size());
            String target = "n" + (int) (Math.random() * numNodes);
            
            int value = (int) (Math.random() * maxEdgeValue);
            
            Edge edge = new Edge("e_" + i, String.valueOf(source), String.valueOf(target), value);
            
            graph.addEdge(edge);
        }
        
        return graph;
    }
    
    
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
    
    @Override
    public String toString() {
        return toJSONObject().toString(3);
    }
}
