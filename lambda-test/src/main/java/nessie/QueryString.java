package nessie;

import java.io.*;
import java.util.*;
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


public class QueryString implements RequestHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> data, Context context) {
        System.out.println(data);
        return "Success";
    }
}