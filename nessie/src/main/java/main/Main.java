package main;


import linker.Linker;
import treeBuilders.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Created by joeraso on 5/3/16.
 */
public class Main {

    public static void main(String[] args) {
        Linker linker = new Linker();
        TreeBuilder builder;

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
        else {
            builder = new TikaTreeBuilder();
        }

        TreeNode tree = builder.build(new File(filename));
        // TODO: Committing tree to database

        // Step 3: Link this new tree file with the rest of the files
        linker.createLinks(tree);
    }

}
