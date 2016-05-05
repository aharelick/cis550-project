package main;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import dbWrapper.DBWrapper;
import dbWrapper.TreeNode;
import linker.Linker;
import treeBuilders.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.log4j.Logger;

public class UploadPipeline implements RequestHandler<S3Event, String> {
    //static final Logger log = Logger.getLogger(UploadPipeline.class);


    @Override
    public String handleRequest(S3Event s3event, Context context) {
        try {
            Linker linker = new Linker();
            TreeBuilder builder = null;
            DBWrapper dbWrapper = new DBWrapper();
            AmazonS3 s3Client = new AmazonS3Client();
            LambdaLogger logger = context.getLogger();

            S3EventNotification.S3EventNotificationRecord record = s3event.getRecords().get(0);
            String srcBucket = record.getS3().getBucket().getName();
            // Object key may have spaces or unicode non-ASCII characters.
            String srcKey = record.getS3().getObject().getKey().replace('+', ' ');

            try {
                srcKey = URLDecoder.decode(srcKey, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // probably do something here
            }

            // Get the S3 Object
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
            String contentType = s3Object.getObjectMetadata().getContentType();
            logger.log("uploaded: " + srcBucket + '/' + srcKey + " of type: " + contentType);


            // Instantiate the correct tree builder
            switch (contentType) {
                case "xml":
                    builder = new XMLTreeBuilder();
                    break;
                case "json":
                    builder = new JSONTreeBuilder();
                    break;
                case "csv":
                    builder = new CSVTreeBuilder();
                    break;
            }

            // Build the tree, create links, and persist
            if (builder != null) {
                Set<TreeNode> tree = builder.build(s3Object.getObjectContent(), srcKey);
                linker.createLinks(tree);
                dbWrapper.insertNodes(tree);
            }
        } catch (Exception e) {
            System.out.println("Error uploading file to database");
        }

        return "Success!";
    }

}
