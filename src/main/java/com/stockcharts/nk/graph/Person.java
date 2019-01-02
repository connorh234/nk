package com.stockcharts.nk.graph;
import java.util.*;
import org.json.JSONObject;

public class Person {

    private final String id;
    private final Set<String> connections;   // unique list of people this person is connected to by photo

    public Person(String id, Set<String> connections) {
        this.id = id;
        this.connections = connections;
    }

    public Person(String id) {
        this.id = id;
        this.connections = new HashSet<>();
    }

    public void addConnection(String id) {
        connections.add(id);
    }
    
    public String getId() {
        return id;
    }

    public Set<String> getConnections() {
        return connections;
    }
    
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
}
