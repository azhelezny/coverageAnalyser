package main;


import org.apache.commons.cli.*;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Main {

    static int samplersCount = 0;
    static int disabledSamplers = 0;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        Options options = new Options();
        Option jmxFilePath = new Option("j", "jmxPath", true, "jmx file/directory path");
        jmxFilePath.setRequired(true);
        options.addOption(jmxFilePath);

        CommandLineParser parser = new BasicParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("JMX coverage analyser", options);
            System.exit(1);
            return;
        }

        String path = cmd.getOptionValue("jmxPath");

        File fileOrDir = new File(path);
        if (fileOrDir.isFile()) {
            analyzeFile(path);
        } else {
            FileFilter fileFilter = new WildcardFileFilter("*.jmx");
            File[] files = fileOrDir.listFiles(fileFilter);
            for (File file : files) {
                analyzeFile(file.getAbsolutePath());
                System.out.println();
            }
        }
    }


    public static void analyzeFile(String filepath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(new File(filepath));

        findAllThreads(doc);
        System.out.println("Filepath:" + filepath);
        System.out.println("Count: " + samplersCount);
        System.out.println("Disabled: " + disabledSamplers);
        System.out.println("Available Coverage: " + (100 - (disabledSamplers * 100) / samplersCount));
        samplersCount = 0;
        disabledSamplers = 0;
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
            if (currentNode.getNodeName().equals("ThreadGroup") || currentNode.getNodeName().equals("SetupThreadGroup"))
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


    public static StringBuilder replaceSelected(String replaceWith, String replaceWhat, File filepath) throws FileNotFoundException {

        String targetStr = replaceWhat;
        String altStr = replaceWith;
        java.io.File file = filepath;
        java.util.Scanner scanner = new java.util.Scanner(file);
        StringBuilder buffer = new StringBuilder();


        // if (scanner.hasNext()) buffer.append("\n");

        while (scanner.hasNext()) {
            buffer.append(scanner.nextLine().replaceAll(targetStr, altStr));
            if (scanner.hasNext()) buffer.append("\n");
        }
        scanner.close();
        return buffer;

    }
}




