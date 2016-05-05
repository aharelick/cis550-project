package treeBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbWrapper.TreeNode;
import org.apache.commons.collections4.IteratorUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by joeraso on 5/3/16.
 */
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

    public Set<TreeNode> build(File file) {
        JsonNode rootNode;
        try {
            graph = new HashSet<>();
            ObjectMapper mapper = new ObjectMapper();
            rootNode = mapper.readTree(file);
        } catch (IOException e) {
            System.out.println("Couldn't read file");
            return null;
        }

        buildRecursive(file.getName(), rootNode);
        return graph;
    }
}
