package com.stockcharts.nk;
import com.stockcharts.nk.aws.S3Utils;
import com.stockcharts.nk.aws.RekognitionCollection;
import com.amazonaws.services.rekognition.*;

import java.io.*;
import java.util.*;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.stockcharts.nk.data.KurokoFaceMatch;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;


import java.io.File;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import com.stockcharts.nk.graph.*;


public class Main {

    private static int NUM_NODES = 10;
    private static int NUM_EDGES = 20;
    
    private static int MAX_EDGE_VALUE = 10;
    
    private static int NUM_PEOPLE = 10;
    private static int MAX_CONNECTIONS = 5;
    
    private static List<Node> nodes = new LinkedList<>();
    private static List<Edge> edges = new LinkedList<>();
    
   
    private static final float FACE_THRESHOLD = 0.99990f;
    private static final float MAX_YAW = 60f;
    private static final AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();

    public static ComboPooledDataSource sqlPool;
    
    public static void main(String[] args) throws Exception {
        
        doCollectionTest();
        
    }
    
    private static void doCollectionTest() throws Exception {
        
        clearTestEnvironment();
        
        for (String imageName : S3Utils.getBucketObjectKeys(Constants.MAIN_BUCKET)) {
            
//            String imageName = "9.png";
            
            long start = System.currentTimeMillis();
            
            System.out.println("========== " + imageName + " ==================================================");
            
            List<KurokoFaceMatch> faceMatch = ImageProcessor.processImage(Constants.MAIN_BUCKET, imageName, FACE_THRESHOLD, MAX_YAW);
        
            
            System.out.println(System.currentTimeMillis() - start + "ms");
            
            for (KurokoFaceMatch match : faceMatch) {
                System.out.println(match);
            }
        
        }

        System.out.println("========== Collection Ids ==================================================");
        for (String collectionId : RekognitionCollection.getCollectionIds()) {
            System.out.println(collectionId);
        }
        
    }
    
    private static void clearTestEnvironment() throws IOException {
        
        S3Utils.clearBucket(Constants.DETAIL_BUCKET);
        
        for (String collection : RekognitionCollection.getCollectionIds()) {
            RekognitionCollection.deleteCollection(collection);
        }
    }
    
     
    
    
    
}
