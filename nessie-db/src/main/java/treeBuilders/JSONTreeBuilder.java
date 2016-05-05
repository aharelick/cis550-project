package treeBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbWrapper.TreeNode;
import org.apache.commons.collections4.IteratorUtils;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class JSONTreeBuilder implements TreeBuilder {
    private Set<TreeNode> graph;
    private TreeNode parentTreeNode;

    public void buildRecursive(String key, JsonNode node) {

        if (node.isObject()) {
            TreeNode currentTreeNode = createNode(key, "object");

            // Iterate through all of the children
            List<String> fieldNames = IteratorUtils.toList(node.fieldNames());
            for (String field : fieldNames) {
                parentTreeNode = currentTreeNode;
                buildRecursive(field, node.get(field));
            }
        }
        else if (node.isArray()){
            TreeNode currentTreeNode = createNode(key, "array");

            int i = 0;
            for (JsonNode child : node) {
                parentTreeNode = currentTreeNode;
                buildRecursive(key + "/" + i, child);
                i++;
            }
        }
        else {
            createNode(key, node.textValue());
        }
    }

    private TreeNode createNode(String key, String value) {
        TreeNode currentTreeNode = new TreeNode(key, value);
        graph.add(currentTreeNode);

        if (parentTreeNode != null) {
            parentTreeNode.adj.add(currentTreeNode.id);
            currentTreeNode.adj.add(parentTreeNode.id);
        }

        return currentTreeNode;
    }

    @Override
    public Set<TreeNode> build(File file) {
        try {
            return build(new FileInputStream(file), file.getName());
        } catch (IOException e) {
            System.out.println("Couldn't Read file and Create Tree");
        }
        return null;
    }

    @Override
    public Set<TreeNode> build(InputStream stream, String filename) {
        JsonNode rootNode;
        try {
            graph = new HashSet<>();
            ObjectMapper mapper = new ObjectMapper();
            rootNode = mapper.readTree(new BufferedReader(new InputStreamReader(stream)));
        } catch (IOException e) {
            System.out.println("Couldn't read file");
            return null;
        }

        buildRecursive(filename, rootNode);
        return graph;
    }
}
