package main;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;


public class Main {

    static int samplersCount = 0;
    static int disabledSamplers = 0;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File("/Users/azhelezny/projects/splice_machine/test-jmeter/src/test/jmeter/poc-hartehanks.jmx"));

        findAllThreads(doc);

        System.out.println("Count: " + samplersCount);
        System.out.println("Disabled: " + disabledSamplers);
        System.out.println("Available Coverage: " + (100 - (disabledSamplers * 100) / samplersCount));
    }

    public static boolean isEnabled(Node node) {
        if (!node.hasAttributes())
            return true;
        Node pish = node.getAttributes().getNamedItem("enabled");
        return pish.getNodeValue().equals("true");
    }

    public static void findAllThreads(Node node) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i);
            if (currentNode.getNodeName().equals("ThreadGroup"))
                diveInto(currentNode.getNextSibling().getNextSibling(), isEnabled(currentNode));
            findAllThreads(currentNode);
        }
    }


    public static void diveInto(Node group, boolean enabled) {
        NodeList list = group.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i);
            if (currentNode.getNodeName().equals("JDBCSampler")) {
                samplersCount += 1;
                if (!(isEnabled(currentNode) && enabled))
                    disabledSamplers += 1;
            }
            if (currentNode.getNodeName().contains("#")) {
                diveInto(currentNode, enabled);
            }
            if (currentNode.getNodeName().contains("Controller")) {
                diveInto(currentNode.getNextSibling().getNextSibling(), isEnabled(currentNode) && enabled);
            }
        }
    }
}