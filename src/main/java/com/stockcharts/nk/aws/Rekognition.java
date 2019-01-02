package com.stockcharts.nk.aws;

import java.io.*;
import java.nio.*;
import java.util.*;
import javax.imageio.*;
import java.awt.image.*;

import com.amazonaws.services.rekognition.*;
import com.amazonaws.services.rekognition.model.*;

/**
 * Rekognition class provides convenience methods which encapsulate Rekognition API calls
 * @author connorh
 */
public class Rekognition {

    public static final AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
    
    public static List<FaceDetail> detectFacesInImage(String bucket, String photo) throws AmazonRekognitionException {     

        DetectFacesRequest request = new DetectFacesRequest()
            .withImage(new Image()
                .withS3Object(new S3Object()
                    .withName(photo)
                    .withBucket(bucket)))
           .withAttributes(Attribute.DEFAULT);

        DetectFacesResult result = rekognitionClient.detectFaces(request);
        
        return result.getFaceDetails();
    }

    public static boolean isMatch(String bucket, String sourceImg, String targetImg, float similarityThreshold) throws IOException {   
        return isMatch(bucket, S3Utils.getS3Image(bucket, sourceImg), S3Utils.getS3Image(bucket, targetImg), similarityThreshold);
    } 
    
    public static boolean isMatch(String bucket, BufferedImage source, BufferedImage target, float similarityThreshold) throws IOException {
        return isMatch(new Image().withBytes(getBytes(source)), new Image().withBytes(getBytes(target)), similarityThreshold);
    }

    private static boolean isMatch(Image source, Image target, float similarityThreshold) {
        
        CompareFacesRequest request = new CompareFacesRequest()
            .withSourceImage(source)
            .withTargetImage(target)
            .withSimilarityThreshold(similarityThreshold);

        CompareFacesResult compareFacesResult= rekognitionClient.compareFaces(request);

        List <CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
        return !faceDetails.isEmpty();
    }
    
    private static ByteBuffer getBytes(BufferedImage img) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            baos.flush();
            return ByteBuffer.wrap(baos.toByteArray());
        }	
    }
}
