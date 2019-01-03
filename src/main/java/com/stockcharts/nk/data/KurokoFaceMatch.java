package com.stockcharts.nk.data;
import com.amazonaws.services.rekognition.model.BoundingBox;
import java.io.Serializable;

import java.sql.*;
import java.util.*;
import org.json.JSONObject;

public class KurokoFaceMatch {

    private final String id;

    private final String imageId;     // Source imageId
    private final String faceId;    // Matched face
    
    private final BoundingBox boundingBox;
    
    public KurokoFaceMatch(String id, String image, String faceId, BoundingBox boundingBox) {
        this.id = id;
        this.imageId = image;
        this.faceId = faceId;
        this.boundingBox = boundingBox;
    }

    public String getId() {
        return id;
    }

    public String getImageId() {
        return imageId;
    }

    public String getFaceId() {
        return faceId;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
            
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
            
    @Override
    public String toString() {
        return toJSONObject().toString(3);
    }
    
    private static KurokoFaceMatch fromResultRow(ResultSet rs) throws SQLException {
                
        String id = rs.getString("id");
        String image = rs.getString("imageId");
        String faceId = rs.getString("faceId");
        String boundingBoxString = rs.getString("boundingBox");
        
        JSONObject boundingBoxJO = new JSONObject(boundingBoxString);
        
        BoundingBox boundingBox = new BoundingBox()
                .withTop(boundingBoxJO.getFloat("top"))
                .withLeft(boundingBoxJO.getFloat("left"))
                .withHeight(boundingBoxJO.getFloat("height"))
                .withWidth(boundingBoxJO.getFloat("width"));
        
        return new KurokoFaceMatch(id, image, faceId, boundingBox);
    }
    
    public static List<KurokoFaceMatch> fromResultSet(ResultSet rs) throws SQLException {
        List<KurokoFaceMatch> matches = new LinkedList<>();
        while (rs.next()) {
            matches.add(fromResultRow(rs));
        }
        return matches;
    }    
  
}
