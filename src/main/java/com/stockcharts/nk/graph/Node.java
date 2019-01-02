package com.stockcharts.nk.graph;
import java.util.*;
import org.json.JSONObject;

public class Node<T> {

    private final String id;
    private final T data;
    
    private float x;
    private float y;
    
    private int size = 1;
    
    private final List<String> edges;
    
    public Node(String key, T data, List<String> edges) {
        this.x = (float) Math.random();
        this.y = (float) Math.random();
        this.id = key;
        this.data = data;
        this.edges = edges;
    }
      
    public Node(String key, T data) {
        this(key, data, new LinkedList<>());
    }
       
    public void addEdge(String edgeId) {
        edges.add(edgeId);
    }
    
    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public List<String> getEdges() {
        return edges;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
    
    public JSONObject toJSONObject() {
        JSONObject jo = new JSONObject(this);
        return jo;
    }
    
    
    @Override
    public String toString() {
        return toJSONObject().toString(3);
    }
    
}
