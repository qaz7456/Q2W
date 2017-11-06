package tw.com.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlUtil {
	private static final Logger logger = LogManager.getLogger(XmlUtil.class);

	public static Document getDocument(String path) {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}
		File file = new File(path);
		Document document = null;
		try {
			document = dombuilder.parse(file);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		return document;
	}
}
