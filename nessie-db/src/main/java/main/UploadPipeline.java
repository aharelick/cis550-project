package main;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import dbWrapper.*;
import linker.Linker;
import treeBuilders.*;

import java.io.File;
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
            DBWrapperInverted dbWrapperInverted = new DBWrapperInverted();
            AmazonS3 s3Client = new AmazonS3Client();

            // If table doesn't exist yet, then create it
            if (!dbWrapper.tableExists("NessieDataTest")) {
                dbWrapper.createTableFromTreeNode();
            }

            // If inverted index doesn't exist yet, then create it
            if (!dbWrapperInverted.tableExists("InvertedDataTest")) {
                dbWrapperInverted.createTableFromInvertedNode();
            }

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
                case "text/xml":
                    builder = new XMLTreeBuilder();
                    break;
                case "application/json":
                    builder = new JSONTreeBuilder();
                    break;
                case "text/csv":
                    builder = new CSVTreeBuilder();
                    break;
            }

            // Build the inverted and forward indices and create links to other trees
            if (builder != null) {
                Set<TreeNode> tree = builder.build(s3Object.getObjectContent(), srcKey);
                Set<InvertedNode> inverted = InvertedBuilder.build(tree);
                linker.createLinks(tree);
                dbWrapperInverted.insertNodes(inverted);
                dbWrapper.insertNodes(tree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Success!";
    }

    public static void main(String[] args) {
        UploadPipeline pipe = new UploadPipeline();
        pipe.handleRequest(null,null);
    }
}
