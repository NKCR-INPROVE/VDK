package cz.incad.vdkcommon.xml;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import cz.incad.vdkcommon.xml.VDKNamespaceContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Alberto
 */
public class XMLReader {

    Logger logger = Logger.getLogger(this.getClass().getName());
    public String separator = " - ";
    private XPath xpath;
    private Document doc;
    boolean nsAware = false;
    DocumentBuilder builder;

    public XMLReader() throws ParserConfigurationException {
        nsAware = true;

            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            xpath.setNamespaceContext(new VDKNamespaceContext());
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(nsAware); // never forget this!
            builder = domFactory.newDocumentBuilder();
    }

    public void loadXml(String xml) throws ParserConfigurationException, SAXException, IOException {

            InputSource source = new InputSource(new StringReader(xml));
            doc = builder.parse(source);
    }
    
    public Document getDoc(){
        return doc;
    }

    public void loadXmlFromFile(File file) {
        try {

            InputSource source = new InputSource(new FileInputStream(file));
            doc = builder.parse(source);

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Can't load xml: {0}", ex.getMessage());
        }
    }

    public void readUrl(String urlString) throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
            
            URL url = new URL(urlString);
            InputStream stream = url.openStream();
            doc = builder.parse(stream);
    }

    public NodeList getListOfNodes(String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

    }

    public NodeList getListOfNodes(Node node, String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);
        return (NodeList) expr.evaluate(node, XPathConstants.NODESET);

    }

    public String[] getListOfValues(Node node, String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {


        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(node, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        String[] s = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            //s.append(nodes.item(i).getNodeValue());
            s[i] = nodes.item(i).getNodeValue();
        }
        return s;
    }

    public String[] getListOfValues(String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {


        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        String[] s = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            //s.append(nodes.item(i).getNodeValue());
            s[i] = nodes.item(i).getNodeValue();
        }
        return s;
    }
    
    public String getNodeText(String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);

        /*
        Object result = expr.evaluate(doc, XPathConstants.NODE);
        Node node = (Node) result;
        if(node!=null)
        return node.getNodeValue();
        else return "";
         */

        Object result = expr.evaluate(doc, XPathConstants.STRING);
        if(result==null){
            return "";
        }else{
            return result.toString();
        }

    }

    public String getNodeValue(String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            //s.append(nodes.item(i).getNodeValue());
            s.append(nodes.item(i).getNodeValue()).append(separator);
        }
        int pos = s.lastIndexOf(separator);
        if (pos > 0) {
            s.delete(pos, s.length());
        }
        return s.toString();

    }

    public String getNodeValue(Node node, String xPath)
            throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {

        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(node, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            //s.append(nodes.item(i).getNodeValue());
            s.append(nodes.item(i).getNodeValue()).append(separator);
        }
        int pos = s.lastIndexOf(separator);
        if (pos > 0) {
            s.delete(pos, s.length());
        }
        return s.toString();

    }

    public Node getNodeElement() {
        return (Node)this.doc.getDocumentElement();
    }

    ArrayList<String> getNodeValues(Node node, String xPath) throws XPathExpressionException {
        ArrayList<String> s = new ArrayList<String>();
        XPathExpression expr = xpath.compile(xPath);

        Object result = expr.evaluate(node, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
            s.add(nodes.item(i).getNodeValue());
        }
        return s;
    }
}
