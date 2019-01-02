package com.stockcharts.nk.graph;
import java.util.*;

public interface EdgeCalculator<T> {

    public List<Edge> calculateEdges(Collection<Node<T>> data);
    
}
