package com.stockcharts.nk.data;

import java.sql.*;
import java.util.*;
import org.json.JSONObject;

public class Face {

    private final String id;
    private final String collectionId;
    private final String name;

    public Face(String id, String collectionId, String name) {
        this.id = id;
        this.collectionId = collectionId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public String getName() {
        return name;
    }
    
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
            
    @Override
    public String toString() {
        return toJSONObject().toString(3);
    }
    
    private static Face fromResultRow(ResultSet rs) throws SQLException {
                
        String id = rs.getString("id");
        String collectionId = rs.getString("collectionId");
        String name = rs.getString("name");
    
        return new Face(id, collectionId, name);
    }
    
    public static List<Face> fromResultSet(ResultSet rs) throws SQLException {
        List<Face> matches = new LinkedList<>();
        while (rs.next()) {
            matches.add(fromResultRow(rs));
        }
        return matches;
    } 
    
}
