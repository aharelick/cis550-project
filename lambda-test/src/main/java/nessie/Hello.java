package nessie;

import java.io.*;
import java.net.URLDecoder;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.lambda.runtime.events.S3Event;


public class Hello implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        S3EventNotificationRecord record = s3event.getRecords().get(0);
        String srcBucket = record.getS3().getBucket().getName();
        // Object key may have spaces or unicode non-ASCII characters.
        String srcKey = record.getS3().getObject().getKey().replace('+', ' ');
        try {
            srcKey = URLDecoder.decode(srcKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // probably do something here
        }
        LambdaLogger logger = context.getLogger();

        AmazonS3 s3Client = new AmazonS3Client();
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
        ObjectMetadata s3ObjectMetadata = s3Object.getObjectMetadata();
        String contentType = s3ObjectMetadata.getContentType();
        // InputStream objectData = s3Object.getObjectContent();
        logger.log("uploaded: " + srcBucket + '/' + srcKey + " of type: " + contentType);
        return srcBucket + '/' + srcKey;
    }
}