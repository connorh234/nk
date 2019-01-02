package com.stockcharts.nk.aws;

import java.io.*;
import java.util.*;

import com.amazonaws.services.rekognition.*;
import com.amazonaws.services.rekognition.model.*;

/**
 * RekognitionCollection class provides convenience methods for working with Rekognition Collections
 * @author connorh
 */
public class RekognitionCollection {
    
    private static final AmazonRekognition rekognitionClient = Rekognition.rekognitionClient;

    public static boolean hasFaceMatchInCollection(String bucket, String imageName, String collectionName, float confidenceThreshold) throws IOException {
        return !getMatchingFacesInCollection(bucket, imageName, collectionName, confidenceThreshold).isEmpty();
    }
    
    public static List<FaceMatch> getMatchingFacesInCollection(String bucket, String imageName, String collectionName, float confidenceThreshold) throws IOException {
       
        System.out.println("Lookig for match for " + imageName);
        
        // Get an image object from S3 bucket.
        Image image = new Image()
            .withS3Object(new S3Object()
            .withBucket(bucket)
            .withName(imageName));

        // Search collection for faces similar to the largest face in the image.
        SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
            .withCollectionId(collectionName)
            .withImage(image)
            .withFaceMatchThreshold(confidenceThreshold)
            .withMaxFaces(2);

        SearchFacesByImageResult searchFacesByImageResult = rekognitionClient.searchFacesByImage(searchFacesByImageRequest);
        
        return searchFacesByImageResult.getFaceMatches();
    }
    
    public static List<Face> listFacesInCollection(String collectionId) throws IOException {
        
        ListFacesResult result = null;                
        List<Face> faces = new LinkedList<>();
        
        do {
            String paginationToken = (result == null) ? null : result.getNextToken();
            
            ListFacesRequest listFacesRequest = new ListFacesRequest()
                .withCollectionId(collectionId)
                .withMaxResults(10)
                .withNextToken(paginationToken);

            result = rekognitionClient.listFaces(listFacesRequest);
            
            faces.addAll(result.getFaces());
           
        } while (result.getNextToken() !=null); 

        return faces;
    }
    
    public static void indexPhoto(String bucket, String photoName, String collectionId, int maxFaces) {
          
        IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
            .withImage(new Image()
                .withS3Object(new S3Object()
                .withName(photoName)
                .withBucket(bucket)))
            .withQualityFilter(QualityFilter.AUTO)
            .withMaxFaces(maxFaces)
            .withCollectionId(collectionId)
            .withExternalImageId(photoName)
            .withDetectionAttributes("DEFAULT");
        
        IndexFacesResult indexFacesResult = rekognitionClient.indexFaces(indexFacesRequest);
        
        // Iterate through results - useful for analysis of index requests.
//        for (FaceRecord faceRecord : indexFacesResult.getFaceRecords()) {
//        }
//        List<UnindexedFace> unindexedFaces = indexFacesResult.getUnindexedFaces();
//        for (UnindexedFace unindexedFace : unindexedFaces) {
//            for (String reason : unindexedFace.getReasons()) {
//            }
//        }
    }
    
    public static List<String> getCollectionIds() {
    
        ListCollectionsResult result = null;        
        List<String> collections = new LinkedList<>();
        
        do {
            String paginationToken = (result == null) ? null : result.getNextToken();
            
            ListCollectionsRequest request = new ListCollectionsRequest()
                    .withNextToken(paginationToken)
                    .withMaxResults(10);
            
            result = rekognitionClient.listCollections(request);
            collections.addAll(result.getCollectionIds());
            
        } while (result.getNextToken() !=null);
        
        return collections;
    }
    
    
    public static void createCollection(String collectionId) {     
        rekognitionClient.createCollection(new CreateCollectionRequest().withCollectionId(collectionId));
    }
    
    public static void deleteCollection(String collectionId) {
        rekognitionClient.deleteCollection(new DeleteCollectionRequest().withCollectionId(collectionId));
    }
      
}
