package com.stockcharts.nk.data;
import com.stockcharts.nk.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class FaceDAO {

    public static void putFace(Face face) throws SQLException {
        
    }
    
    public static Face getFace(String id) throws SQLException {
        
        String query = "SELECT * FROM connorh.Faces WHERE id = ?";
        
        try (Connection conn = Main.sqlPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, id);
            
            // This is sketchy AF
            return Face.fromResultSet(stmt.executeQuery()).get(0);
        }
    }
    public static List<Face> getAllFaces()throws SQLException {
        
        String query = "SELECT * FROM connorh.Faces";
        
        try (Connection conn = Main.sqlPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            
            return Face.fromResultSet(stmt.executeQuery());
        }
    }
}
