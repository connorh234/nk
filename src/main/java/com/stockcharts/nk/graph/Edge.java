package com.stockcharts.nk.graph;
import java.util.*;
import org.json.JSONObject;

public class Edge {

    private final String id;
    
    private final String source;
    private final String target;
    
    private int size;

    public Edge(String key, String node1, String node2, int value) {
        this.id = key;
        this.source = node1;
        this.target = node2;
        this.size = value;
    }
    
    public Edge(String key, String node1, String node2) {
        this.id = key;
        this.source = node1;
        this.target = node2;
        this.size = 0;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public int getSize() {
        return size;
    }
    
    public int incrementSize() {
        return ++size;
    }
    
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
    
    @Override
    public String toString() {
        return toJSONObject().toString(3);
    }
}
