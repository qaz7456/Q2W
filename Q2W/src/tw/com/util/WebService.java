package tw.com.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tw.com.bean.WebServiceBean;

public class WebService {

	private static final Logger logger = LogManager.getLogger(WebService.class);
	
	public static String execute(String message) throws Exception {
		logger.debug("WebService FILE_XML_PATH: {}", Q2W.FILE_XML_PATH);
		logger.debug("WebService CONVERT_XML_PATH: {}", Q2W.CONVERT_XML_PATH);
		
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}
		File file = new File(Q2W.FILE_XML_PATH);
		Document configDoc = null;
		try {
			configDoc = dombuilder.parse(file);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		Element configRoot = configDoc.getDocumentElement();

		NodeList webService = configRoot.getElementsByTagName("webService");
		NodeList webServiceInfo = webService.item(0).getChildNodes();
		
		String format = null;
		String type = null;
		String url = null;
		
		for (int i = 0; i < webServiceInfo.getLength(); i++) {
			Node node = (Node) webServiceInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				
				NamedNodeMap namedNodeMap = node.getAttributes();
				for (int l = 0; l < namedNodeMap.getLength(); ++l) {
					Node attr = namedNodeMap.item(l);
					String attrName = attr.getNodeName();
					String attrVal = attr.getNodeValue();

					format = "format".equals(attrName) ? attrVal : format;
					type = "type".equals(attrName) ? attrVal : type;
					url = "url".equals(attrName) ? attrVal : url;
				}
			}
		}
		logger.debug("format: {} \\ type: {} \\ url: {}", format, type,
				url);		
		
		
		
//		ApplicationContext context = new ClassPathXmlApplicationContext(Q2W.FILE_XML_PATH);
		
//		WebServiceBean webServiceBean = (WebServiceBean) context.getBean("webService");
		HttpEntity responseEntity = null;
		String response = null;

//		String format = webServiceBean.getFormat();
//		String type = webServiceBean.getType();
//		String url = webServiceBean.getUrl();
//
//		logger.debug("format: {}", format);
//		logger.debug("type: {}", type);
//		logger.debug("url: {}", url);
		
		HttpClient httpClient = HttpClients.createDefault();

		if ("xml".equalsIgnoreCase(format)) {
			format = "text/xml";
			message = XMLConverter.getXml(message,Q2W.CONVERT_XML_PATH);
		}
		if ("json".equalsIgnoreCase(format)) {
			format = "application/json";
			message = XMLConverter.getJson(message,Q2W.CONVERT_XML_PATH);
		}

		logger.debug("轉換: {}", message);
		
		if ("get".equalsIgnoreCase(type)) {
			URI uri = new URIBuilder(url)
					.addParameter("logistics_interface", new String(message.getBytes("utf-8"), "utf-8")).build();
			HttpGet httpRequest = new HttpGet(uri);

			httpRequest.setHeader("Content-Type", format);
			httpRequest.addHeader("charset", "utf-8");

			HttpResponse httpResponse = httpClient.execute(httpRequest);

			responseEntity = httpResponse.getEntity();
		}
		if ("post".equalsIgnoreCase(type)) {

			HttpPost httpRequest = new HttpPost(url);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("logistics_interface", message));

			HttpEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);

			for (Header s : httpRequest.getAllHeaders()) {
				logger.debug("Header[" + s + "]");
			}

			httpRequest.setEntity(entity);
			httpRequest.setHeader("Content-Type", format);
			httpRequest.addHeader("charset", "utf-8");

			HttpResponse httpResponse = httpClient.execute(httpRequest);

			responseEntity = httpResponse.getEntity();

		}
		if (responseEntity != null) {
			response = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
			logger.debug("響應: {}", response);
		}
		return response;

	}
}
