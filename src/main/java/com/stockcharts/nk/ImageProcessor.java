package com.stockcharts.nk;

import com.stockcharts.nk.aws.S3Utils;
import com.stockcharts.nk.aws.Rekognition;
import com.stockcharts.nk.aws.RekognitionCollection;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.awt.image.*;

import com.stockcharts.nk.data.Face;
import com.stockcharts.nk.data.FaceDAO;
import com.stockcharts.nk.data.FaceMatch;
import com.stockcharts.nk.data.FaceMatchDAO;

import com.amazonaws.services.rekognition.model.*;
import org.json.JSONObject;

/**
 * Class provides a single public method which matches all faces detected in photo provided.
 * @author connorh
 */
public class ImageProcessor {
    
    /*
        Notes
        - Currently swapping to FaceMatch model where a single large collection is used & the mode of all matched faces is selected
        - Will have implications on when/how faces are indexed
        - Will have implication on when/how FaceMatches are persisted
        - Will want to program "re-analysis" capabilities into system
            - Go back and re-assign matches if underlying data has changed enough to warrant better match
    */
 
    public static List<FaceMatch> processImage(String bucketName, String sourceImageName, float faceSimilarityThreshold, float maxYaw) throws IOException, SQLException {
           
        // Load source photo from S3
        BufferedImage srcImage = S3Utils.getS3Image(bucketName, sourceImageName);            
        
        List<FaceMatch> faceMatches = new LinkedList<>();
        
        List<FaceDetail> detectedFaces = Rekognition.detectFacesInImage(bucketName, sourceImageName);
        
        detectedFaces = filterDetectedFaces(detectedFaces, maxYaw);
        
        for (FaceDetail faceDetectedInImage : detectedFaces) {
            faceMatches.add(processDetectedFace(faceDetectedInImage, srcImage, sourceImageName, faceSimilarityThreshold));
        }
        
        return faceMatches;
        
    }
    
    private static List<FaceDetail> filterDetectedFaces(List<FaceDetail> sourceFaces, float maxYaw) {
        
        List<FaceDetail> faces = new LinkedList<>();
        
        for (FaceDetail face : sourceFaces) {
            
            System.out.println(new JSONObject(face).toString(3));
            
            if (Math.abs(face.getPose().getYaw()) > maxYaw) {
                System.out.println("Yaw Too High: " + face.getPose().getYaw());
                continue;
            } else {
                faces.add(face);
            }
            
            
        } 
        
        return faces;
    }
    
    private static FaceMatch processDetectedFace(FaceDetail faceDetectedInImage, BufferedImage srcImage, String sourceImageName, float faceSimilarityThreshold) throws IOException {
        
        
        // Generate cropped photo of identified face
        BufferedImage faceDetailImage = getBoundingBoxSubImage(srcImage, faceDetectedInImage);
            
        // Save cropped img to S3
        String detailImageId = UUID.randomUUID().toString() + ".png";
        S3Utils.putImg(Constants.DETAIL_BUCKET, detailImageId, faceDetailImage);

        // Sam - this commented out block is going to be the new face matching methodology.  What exists here is an older version which I'm ditching
        List<com.amazonaws.services.rekognition.model.FaceMatch> matchingFaces = RekognitionCollection.getMatchingFacesInCollection(Constants.DETAIL_BUCKET, detailImageId, Constants.TEST_COLLECTION, faceSimilarityThreshold);
        
        Set<String> sourceImages = new HashSet<>();
        for (com.amazonaws.services.rekognition.model.FaceMatch match : matchingFaces) {
            sourceImages.add(match.getFace().getExternalImageId());
        }
        
        List<FaceMatch> matchingEntries;
        try {
            matchingEntries = FaceMatchDAO.getFaceMatchesForImages(sourceImages);
        } catch (SQLException e) {
            return null;
        }
        
        Map<String,Integer> faceOccurances = countFaceOccurrances(matchingEntries);
        
        String matchingFaceId;
        if (faceOccurances.isEmpty()) {
            matchingFaceId = UUID.randomUUID().toString(); 
            Face face = new Face(matchingFaceId, "unknown");
            try {
                FaceDAO.putFace(face);
            } catch (SQLException e) {
                
            }
        } else {
            List<Map.Entry<String, Integer>> sortedFaceSet = new LinkedList<>(faceOccurances.entrySet());
            
            Collections.sort(sortedFaceSet, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            matchingFaceId = sortedFaceSet.get(0).getKey();
        }
        
        // Index new photo into the collection
        RekognitionCollection.indexPhoto(Constants.DETAIL_BUCKET, detailImageId, Constants.TEST_COLLECTION, 1);
        
        // Create & persist new FaceMatch object
        FaceMatch faceMatch = new FaceMatch(UUID.randomUUID().toString(), sourceImageName, matchingFaceId, faceDetectedInImage.getBoundingBox());
        try {
            FaceMatchDAO.putFaceMatch(faceMatch);
        } catch (SQLException e) {

        }
        
        return faceMatch;
    }
    
    private static Map<String, Integer> countFaceOccurrances(List<FaceMatch> faceMatches) {
        
        Map<String, Integer> tallies = new HashMap<>();
        
        for (FaceMatch match : faceMatches) {
            
            String faceId = match.getFaceId();
            
            if (tallies.containsKey(faceId) == false) {
                tallies.put(faceId, 0);
            }
            
            int tally = tallies.get(faceId);
            tallies.put(faceId, ++tally);
        }
        
        return tallies;
    }
    
    private static BufferedImage getBoundingBoxSubImage(BufferedImage sourceImage, FaceDetail face) {
        
        int imgHeight = sourceImage.getHeight();
        int imgWidth = sourceImage.getWidth();
        
        BoundingBox box = face.getBoundingBox();
    
        // All bounding box coordinates/sizes are expressed as a ratio of 1 (i.e. 0.75)
        
        float leftRatio = box.getLeft();
        float topRatio = box.getTop();

        float heightRatio = box.getHeight();
        float widthRatio = box.getWidth();

        int left = (int)(leftRatio * imgWidth);
        int top = (int)(topRatio * imgHeight);

        int boxWidth = (int)(widthRatio * imgWidth);
        int boxHeight = (int)(heightRatio * imgHeight);
            
        System.out.println(left + " " + top + " " + boxWidth + " " + boxHeight);
        
        BufferedImage subImg = sourceImage.getSubimage(left, top, boxWidth, boxHeight);
        
        return subImg;
    }
    
}
