package main;


import dbWrapper.DBWrapper;
import dbWrapper.TreeNode;
import linker.Linker;
import treeBuilders.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;

import org.apache.log4j.Logger;

/**
 * Created by joeraso on 5/3/16.
 */
public class Main {
    //static final Logger log = Logger.getLogger(Main.class);


    public static void main(String[] args) throws Exception {
        //log.debug(args[0]);
        //log.debug("log data from log4j debug \n this is continuation of log4j debug");

        Linker linker = new Linker();
        TreeBuilder builder;
        DBWrapper dbWrapper = new DBWrapper();

        // Step 1: Pull new file from S3


        // Step 2: Instantiate the correct builder and build a tree
        String filename = args[0];
        String extension = FilenameUtils.getExtension(filename);
        if (extension.equals("xml")) {
            builder = new XMLTreeBuilder();
        }
        else if (extension.equals("json")) {
            builder = new JSONTreeBuilder();
        }
        else if (extension.equals("csv")) {
            builder = new CSVTreeBuilder();
        }
        else {
            builder = new TikaTreeBuilder();
        }

        Set<TreeNode> tree = builder.build(new File(filename));
        linker.createLinks(tree);
        dbWrapper.insertNodes(tree);
    }

}
