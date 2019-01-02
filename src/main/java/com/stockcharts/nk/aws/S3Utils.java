package com.stockcharts.nk.aws;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;

/**
 * S3Utils class provides convenience methods which encapsulate S3 API calls
 * @author connorh
 */
public class S3Utils {

    private static final AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                    .build();
    
/* ========== GET ========== */
    public static S3Object getS3Object(String bucket, String file) {
        return s3client.getObject(new GetObjectRequest(bucket, file));
    }
    
    public static BufferedImage getS3Image(String bucket, String file) throws IOException {
        S3Object s3object = getS3Object(bucket, file);
        S3ObjectInputStream inputStream = s3object.getObjectContent();        
        return ImageIO.read(inputStream);
    }
    
    public static List<String> getBucketObjectKeys(String bucket) throws IOException {
        List<String> objectKeys = new LinkedList<>();
        ObjectListing result = s3client.listObjects(new ListObjectsRequest().withBucketName(bucket));
        for (S3ObjectSummary summary : result.getObjectSummaries()) {
            objectKeys.add(summary.getKey());
        }
        return objectKeys;
    }
    
/* ========== PUT ========== */
    public static final void putImg(String bucket, String name, BufferedImage img) throws IOException {
        
        name = name.replace(".jpg", ".png");
        if (name.endsWith(".png") == false) name += ".png";
        
        File file = new File("/var/tmp/" + UUID.randomUUID().toString() + ".png");
        ImageIO.write(img, "png", file);
        s3client.putObject(bucket, name, file);
        file.delete();
    }
    
/* ========== DELETE ========== */
    public static void deleteObject(String bucketName, String key) {
        s3client.deleteObject(bucketName, key);
    }
    
    public static void clearBucket(String bucketName) throws IOException {
        for (String key : getBucketObjectKeys(bucketName)) {
            deleteObject(bucketName, key);
        }
    }

}
