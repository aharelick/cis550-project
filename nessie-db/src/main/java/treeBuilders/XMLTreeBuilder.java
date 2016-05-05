package treeBuilders;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dbWrapper.TreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLTreeBuilder implements TreeBuilder {
	private Set<TreeNode> graph;
	private TreeNode parentTreeNode;
	private String filename;

	public Set<TreeNode> build(File file) {
		try {
			return build(new FileInputStream(file), file.getName());
		} catch (IOException e) {
			System.out.println("Couldn't Read File In");
		}
		return null;
	}

	public Set<TreeNode> build(InputStream stream, String filename) {
		this.filename = filename;
		graph = new HashSet<>();
		DocumentBuilder dbuilder;
		try {
			dbuilder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
		} catch (ParserConfigurationException e) { 
			e.printStackTrace();
			return null; //  TODO: redo
		}
		Document doc;
        try {

			doc = dbuilder.parse(stream);
		} catch (SAXException | IOException e) {
			return null;
		}
        
        Element root = doc.getDocumentElement();
        generateTree(root);
		return graph;
	}
	
	private void generateTree(Node node) {
		
		String name = node.getNodeName();
		TreeNode root = new TreeNode(
				name.startsWith("#") ? name.substring(1) : name, 
			    node.getNodeValue(), filename
		);
		graph.add(root);
		parentTreeNode.adj.add(root.id);
		root.adj.add(parentTreeNode.id);
		
		// add attributes
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				TreeNode converted = new TreeNode(attr.getNodeName(), attr.getNodeValue(), filename);
				graph.add(converted);

				root.adj.add(converted.id);
				converted.adj.add(root.id);
			}
		}
		
		// add child nodes
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			parentTreeNode = root;
			generateTree(children.item(i));
		}
	}

}