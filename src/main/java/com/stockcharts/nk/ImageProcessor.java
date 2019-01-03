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
import com.stockcharts.nk.data.KurokoFaceMatch;
import com.stockcharts.nk.data.KurokoFaceMatchDAO;

import com.amazonaws.services.rekognition.model.*;
import org.json.JSONObject;

/**
 * Class provides a single public method which matches all faces detected in photo provided.
 * @author connorh
 */
public class ImageProcessor {
    
    /*
        Notes
        - Currently swapping to KurokoFaceMatch model where a single large collection is used & the mode of all matched faces is selected
        - Will have implications on when/how faces are indexed
        - Will have implication on when/how FaceMatches are persisted
        - Will want to program "re-analysis" capabilities into system
            - Go back and re-assign matches if underlying data has changed enough to warrant better match
    */
 
    public static List<KurokoFaceMatch> processImage(String bucketName, String sourceImageName, float faceSimilarityThreshold, float maxYaw) throws IOException, SQLException {
           
        // Load source photo from S3
        BufferedImage srcImage = S3Utils.getS3Image(bucketName, sourceImageName);            
        
        List<KurokoFaceMatch> faceMatches = new LinkedList<>();
        
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
    
    private static KurokoFaceMatch processDetectedFace(FaceDetail faceDetectedInImage, BufferedImage srcImage, String sourceImageName, float faceSimilarityThreshold) throws IOException, SQLException {
        
        BufferedImage faceDetailImage = getBoundingBoxSubImage(srcImage, faceDetectedInImage);
            
        String detailImageId = UUID.randomUUID().toString() + ".png";
        S3Utils.putImg(Constants.DETAIL_BUCKET, detailImageId, faceDetailImage);

        List<FaceMatch> matchingFaces = RekognitionCollection.getMatchingFacesInCollection(Constants.DETAIL_BUCKET, detailImageId, Constants.TEST_COLLECTION, faceSimilarityThreshold);
        
        String matchingFaceId = null;
        
        if (matchingFaces.isEmpty() == false) {
            
            Set<String> faceMatchSourceImageKeys = new HashSet<>();
            for (FaceMatch match : matchingFaces) {
                faceMatchSourceImageKeys.add(match.getFace().getExternalImageId());
            }

            List<KurokoFaceMatch> kurokoFaceMatches = KurokoFaceMatchDAO.getFaceMatchesForImages(faceMatchSourceImageKeys);
            
            matchingFaceId = calculateMatchingFaceFromOccurrenceSet(kurokoFaceMatches);
        
        }
        
        if (matchingFaceId == null) {
            matchingFaceId = UUID.randomUUID().toString(); 
            Face face = new Face(matchingFaceId, "unknown");
            try {
                FaceDAO.putFace(face);
            } catch (SQLException e) {
                // retry(?)
            }
        } 
        
        // Index new photo into the collection
        RekognitionCollection.indexPhoto(Constants.DETAIL_BUCKET, detailImageId, Constants.TEST_COLLECTION, 1);
        
        // Create & persist new KurokoFaceMatch object
        KurokoFaceMatch faceMatch = new KurokoFaceMatch(UUID.randomUUID().toString(), sourceImageName, matchingFaceId, faceDetectedInImage.getBoundingBox());
        try {
            KurokoFaceMatchDAO.putFaceMatch(faceMatch);
        } catch (SQLException e) {

        }
        
        return faceMatch;
    }
    
    private static Map<String, Integer> countFaceOccurrences(List<KurokoFaceMatch> faceMatches) {
                
        Map<String, Integer> tallies = new HashMap<>();
        
        for (KurokoFaceMatch match : faceMatches) {
            
            String faceId = match.getFaceId();
            
            if (tallies.containsKey(faceId) == false) {
                tallies.put(faceId, 0);
            }
            
            int tally = tallies.get(faceId);
            tallies.put(faceId, ++tally);
        }
        
        return tallies;
    }
    
    /* Given a list of KurokoFaceMatches which correspond to Rekognition collection matches, determine the face in the source image */
    private static String calculateMatchingFaceFromOccurrenceSet(List<KurokoFaceMatch> faceMatches) {
        
        if (faceMatches.isEmpty()) return null;
        
        Map<String,Integer> faceOccurrenceCounts = countFaceOccurrences(faceMatches);

        List<Map.Entry<String, Integer>> sortedOccurrenceSet = new LinkedList<>(faceOccurrenceCounts.entrySet());
            
        Comparator<Map.Entry<String, Integer>> MOST_FREQUENT_FIRST = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        };
        
        Collections.sort(sortedOccurrenceSet, MOST_FREQUENT_FIRST);
        
        // TODO - Implement an algorithm to make a more intelligent decision based off of face matches.
        // Currently just selecting the most frequent face.
        
        return sortedOccurrenceSet.get(0).getKey();
    }
    
    /* Generate a cropped image based on coordinates provided by AWS BoundingBox */
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
