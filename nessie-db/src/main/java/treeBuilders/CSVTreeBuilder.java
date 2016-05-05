package treeBuilders;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dbWrapper.TreeNode;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CSVTreeBuilder implements TreeBuilder {
    private String filename;
    private Set<TreeNode> graph;
    private TreeNode parentNode;
    private int recordNum = 1;

    private void addRecord(List<String> headers, List<String> fields) {
        // Create the 'record' node and add it to the list of records
        TreeNode record = new TreeNode("record", filename + "/" + recordNum);

        record.adj.add(parentNode.id);
        parentNode.adj.add(record.id);
        parentNode = record;
        graph.add(record);

        recordNum++;

        // Create all of the field nodes
        List<TreeNode> nodes = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            TreeNode currentNode = new TreeNode(headers.get(i), fields.get(i));
            nodes.add(currentNode);
            graph.add(currentNode);
        }

        // Create a clique of the fields and connect them to the 'record' node
        for (int i = 0; i < nodes.size(); i++) {
            record.adj.add(nodes.get(i).id);
            nodes.get(i).adj.add(record.id);

            for (int j = i + 1; j < nodes.size(); j++) {
                nodes.get(i).adj.add(nodes.get(j).id);
                nodes.get(j).adj.add(nodes.get(i).id);
            }
        }
    }

    public Set<TreeNode> build(File file) {
        try {
            return build(new FileInputStream(file), file.getName());
        } catch (IOException e) {
            System.out.println("Couldn't create tree");
        }
        return null;
    }

    @Override
    public Set<TreeNode> build(InputStream stream, String filename) {
        try {
            // Create the root node
            this.filename = filename;
            graph = new HashSet<>();
            parentNode = new TreeNode("file", filename);
            graph.add(parentNode);

            CSVFormat csvFileFormat = CSVFormat.DEFAULT;
            CSVParser parser = new CSVParser(new BufferedReader(new InputStreamReader(stream)), csvFileFormat);
            List<CSVRecord> records = parser.getRecords();

            // Create the headers
            List<String> headers = IteratorUtils.toList(records.get(0).iterator());

            // Add the records as children to the file
            for (int i = 1; i < records.size(); i++) {
                List<String> fields = IteratorUtils.toList(records.get(i).iterator());
                addRecord(headers, fields);
            }

        } catch (IOException e) {
            System.out.println("Couldn't Read File");
        }

        return graph;
    }
}
