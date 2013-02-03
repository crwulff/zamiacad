package org.zamia.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;

import static org.zamia.util.FileUtils.closeSilently;

/**
 * @author Anton Chepurov
 */
public class XMLUtils {

	private final static ExceptionLogger el = ExceptionLogger.getInstance();
	private final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private static XPath X_PATH;

	private static XPath getX_PATH() {
		if (X_PATH == null) {
			X_PATH = XPathFactory.newInstance().newXPath();
		}
		return X_PATH;
	}

	public static Document parseXML(File aFile) {

		InputStream in = null;

		try {
			in = new FileInputStream(aFile);

			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);

		} catch (FileNotFoundException e) {
			logger.info("### ERROR ### XML file not found: " + aFile.getAbsolutePath());
			el.logException(e);
		} catch (SAXException e) {
			logger.info("### ERROR ### Failed to read XML file '" + aFile.getAbsolutePath() + "': " + e.getMessage());
			el.logException(e);
		} catch (ParserConfigurationException e) {
			logger.info("### ERROR ### Failed to read XML file '" + aFile.getAbsolutePath() + "': " + e.getMessage());
			el.logException(e);
		} catch (IOException e) {
			logger.info("### ERROR ### Failed to read XML file '" + aFile.getAbsolutePath() + "': " + e.getMessage());
			el.logException(e);
		} finally {
			closeSilently(in);
		}
		return null;
	}

	public static Collection<Pair<File, Node>> extractScriptFiles(File aXmlFile, String aProjectBasePath) throws ZamiaException {

		NodeList actions = getNodes("/zamiacad/action", parseXML(aXmlFile));
		ArrayList<Pair<File, Node>> scriptFiles = new ArrayList<Pair<File, Node>>(actions.getLength());
		for (int i = 0; i < actions.getLength(); i++) {
			Node action = actions.item(i);

			String script = getText("settings/script", action);
			if (script == null || script.isEmpty()) {
				continue;
			}

			scriptFiles.add(new Pair<File, Node>(new File(aProjectBasePath, script), action));
		}
		return scriptFiles;
	}

	public static NodeList getNodes(String aXPathExpr, Object aContext) throws ZamiaException {
		try {
			XPathExpression expr = getX_PATH().compile(aXPathExpr);
			return (NodeList) expr.evaluate(aContext, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new ZamiaException("### ERROR ### Failed to evaluate XPath expression '" + aXPathExpr + "': " + e.getMessage());
		}
	}

	public static Node getNode(String aXPathExpr, Object aContext) throws ZamiaException {
		try {
			return (Node) getX_PATH().compile(aXPathExpr).evaluate(aContext, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new ZamiaException("### ERROR ### Failed to evaluate XPath expression '" + aXPathExpr + "': " + e.getMessage());
		}
	}

	public static boolean hasNode(String aXPathExpr, Object aContext) throws ZamiaException {
		return getNode(aXPathExpr, aContext) != null;
	}


	public static String getText(Node aNode) throws ZamiaException {
		return aNode == null ? null : aNode.getTextContent();
	}

	public static String getText(String aXPathExpr, Object aContext) throws ZamiaException {
		NodeList nodes = getNodes(aXPathExpr, aContext);
		return getText(nodes.item(0));
	}

	public static String getAttribute(String aAttrName, Node aNode) {
		return aNode.getAttributes().getNamedItem(aAttrName).getNodeValue();
	}

	public static Element createNodeIn(Node aDestNode, String aNode, Document aXml) {
		Element element = aXml.createElement(aNode);
		aDestNode.appendChild(element);
		return element;
	}

	public static void xml2file(Document xml, File aFile) throws ZamiaException {

		BufferedWriter writer = null;

		try {

			OutputFormat format = new OutputFormat(xml);
			format.setIndenting(true);
			format.setIndent(2);

			writer = new BufferedWriter(new FileWriter(aFile));

			XMLSerializer serializer = new XMLSerializer(writer, format);
			serializer.serialize(xml);
			logger.info("Dumped XML document to %s", aFile.getAbsolutePath());

		} catch (IOException e) {
			throw new ZamiaException("### ERROR ### Failed to write XML to file: " + e.getMessage());
		} finally {
			closeSilently(writer);
		}
	}

}
