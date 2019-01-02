package com.stockcharts.nk.graph;
import java.util.*;

public class PersonEdgeCalculator implements EdgeCalculator{

    
    @Override
    public List<Edge> calculateEdges(Collection data) {
        
        List<Node<Person>> dataList = new LinkedList<>(data);
        
        List<Edge> edges = new LinkedList<>();
        
        for (int i = 0; i < data.size(); i++) {
            for (int j = i; j < data.size(); j++) {
                
                Node<Person> source = dataList.get(i);
                Node<Person> target = dataList.get(j);
                
                if (source.getData().getConnections().contains(target.getData().getId())) {
                    
                    String sourceId = source.getData().getId();
                    String targetId = target.getData().getId();
                    
                    Edge edge = new Edge("e_" + sourceId + "_" + targetId, sourceId, targetId, 1);
                                        
                    edges.add(edge);
                }
            }
        }
        
        return edges;
    }

   

    
}
