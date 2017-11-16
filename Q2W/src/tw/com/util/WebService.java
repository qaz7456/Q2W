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
import tw.com.yahooapi.util.YahooAPI;

public class WebService {

	private static final Logger logger = LogManager.getLogger(WebService.class);

	public static String execute(String message) throws Exception {
		logger.debug("WebService FILE_XML_PATH: {}", Q2W.FILE_XML_PATH);
		logger.debug("WebService CONVERT_XML_PATH: {}", Q2W.CONVERT_XML_PATH);
		logger.debug("WebService execute message: {}", message);

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

		String format = null, type = null, url = null, apiMethod = null, apiVersion = null, apiGroup = null,
				apiAction = null, apiKey = null, sharedSecret = null;

		for (int i = 0; i < webServiceInfo.getLength(); i++) {
			Node node = (Node) webServiceInfo.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				String nodeName = node.getNodeName();
				String value = node.getTextContent();

				// action = "action".equals(nodeName) ? value : action;
				// encode = "encode".equals(nodeName) ? value : encode;
				format = "format".equals(nodeName) ? value : format;
				type = "type".equals(nodeName) ? value : type;
				url = "url".equals(nodeName) ? value : url;

				apiMethod = "apiMethod".equals(nodeName) ? value : apiMethod;
				apiVersion = "apiVersion".equals(nodeName) ? value : apiVersion;
				apiGroup = "apiGroup".equals(nodeName) ? value : apiGroup;
				apiAction = "apiAction".equals(nodeName) ? value : apiAction;
				apiKey = "apiKey".equals(nodeName) ? value : apiKey;
				sharedSecret = "sharedSecret".equals(nodeName) ? value : sharedSecret;
			}
		}
		logger.debug(
				"format: {} \\ type: {} \\ url: {} \\ apiMethod: {} \\ apiVersion: {} \\ apiGroup: {} \\ apiAction: {} \\ apiKey: {} \\ sharedSecret: {}",
				format, type, url, apiMethod, apiVersion, apiGroup, apiAction, apiKey, sharedSecret);

		HttpEntity responseEntity = null;
		String response = null;

		HttpClient httpClient = HttpClients.createDefault();

		// logger.debug("format : " + format);
		// logger.debug("xml.equalsIgnoreCase(format) : " +
		// "xml".equalsIgnoreCase(format));
		// logger.debug("json.equalsIgnoreCase(format) : " +
		// "json".equalsIgnoreCase(format));

		// String new_message = null;

		message = XMLConverter.getRest(format, message, Q2W.CONVERT_XML_PATH, apiKey);

		if ("xml".equalsIgnoreCase(format)) {
			// if ("plain".equalsIgnoreCase(encode)) {
			// message = XMLConverter.getRest(format, message,
			// Q2W.CONVERT_XML_PATH);
			// } else {
			// message = XMLConverter.getRest(format, message,
			// Q2W.CONVERT_XML_PATH, encode);
			// }
			format = "text/xml";

		}
		if ("json".equalsIgnoreCase(format)) {
			// if ("plain".equalsIgnoreCase(encode)) {
			// message = XMLConverter.getRest(format, message,
			// Q2W.CONVERT_XML_PATH);
			// } else {
			// message = XMLConverter.getRest(format, message,
			// Q2W.CONVERT_XML_PATH, encode);
			// }
			format = "application/json";

		}

		message = YahooAPI.getUrl(message, sharedSecret);
		url = url + "/" + apiMethod + "/" + apiVersion + "/" + apiGroup + "/" + apiAction;
		logger.debug("REST format: {}", message);
		logger.debug("url: {}", url);

		String[] params = message.split("&");

		if ("get".equalsIgnoreCase(type)) {
			URIBuilder uriBuilder = new URIBuilder(url);
			for (int i = 0; i < params.length; i++) {
				String[] param = params[i].split("=");
				String key = param[0];
				String value = param[1];
				
				uriBuilder.addParameter(key,
						new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
			}
			URI uri = uriBuilder.build();
			// URI uri = new URIBuilder(url).build();
			// URI uri = new URIBuilder(url).addParameter(apiAction,
			// new String(message.getBytes(StandardCharsets.UTF_8),
			// StandardCharsets.UTF_8)).build();
			HttpGet httpRequest = new HttpGet(uri);

			httpRequest.setHeader("Content-Type", format);
			httpRequest.addHeader("charset", "UTF-8");

			HttpResponse httpResponse = httpClient.execute(httpRequest);

			responseEntity = httpResponse.getEntity();
		}
		// if ("post".equalsIgnoreCase(type)) {
		//
		// HttpPost httpRequest = new HttpPost(url);
		// List<NameValuePair> params = new ArrayList<NameValuePair>();
		// // params.add(new BasicNameValuePair(action, message));
		//
		// HttpEntity entity = new UrlEncodedFormEntity(params,
		// StandardCharsets.UTF_8);
		//
		// for (Header s : httpRequest.getAllHeaders()) {
		// logger.debug("Header[" + s + "]");
		// }
		//
		// httpRequest.setEntity(entity);
		// httpRequest.setHeader("Content-Type", format);
		// httpRequest.addHeader("charset", "UTF-8");
		//
		// HttpResponse httpResponse = httpClient.execute(httpRequest);
		//
		// responseEntity = httpResponse.getEntity();
		//
		// }
		if (responseEntity != null) {
			response = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
		}
		return response;
		// if ("xml".equalsIgnoreCase(format)) {
		// format = "text/xml";
		// message = XMLConverter.getXml(message,Q2W.CONVERT_XML_PATH);
		// }
		// if ("json".equalsIgnoreCase(format)) {
		// format = "application/json";
		// message = XMLConverter.getJson(message,Q2W.CONVERT_XML_PATH);
		// }

		// logger.debug("轉換: {}", message);
		//
		//
		// if ("get".equalsIgnoreCase(type)) {
		// URI uri = new URIBuilder(url)
		// .addParameter("logistics_interface", new
		// String(message.getBytes(StandardCharsets.UTF_8),
		// StandardCharsets.UTF_8)).build();
		// HttpGet httpRequest = new HttpGet(uri);
		//
		// httpRequest.setHeader("Content-Type", format);
		//
		// HttpResponse httpResponse = httpClient.execute(httpRequest);
		//
		// responseEntity = httpResponse.getEntity();
		// }
		// if ("post".equalsIgnoreCase(type)) {
		//
		// HttpPost httpRequest = new HttpPost(url);
		// List<NameValuePair> params = new ArrayList<NameValuePair>();
		// params.add(new BasicNameValuePair("logistics_interface", message));
		//
		// HttpEntity entity = new UrlEncodedFormEntity(params,
		// StandardCharsets.UTF_8);
		//
		// for (Header s : httpRequest.getAllHeaders()) {
		// logger.debug("Header[" + s + "]");
		// }
		//
		// httpRequest.setEntity(entity);
		// httpRequest.setHeader("Content-Type", format);
		// httpRequest.addHeader("charset", "utf-8");
		//
		// HttpResponse httpResponse = httpClient.execute(httpRequest);
		//
		// responseEntity = httpResponse.getEntity();
		//
		// }
		// if (responseEntity != null) {
		// response = EntityUtils.toString(responseEntity,
		// StandardCharsets.UTF_8);
		// logger.debug("響應: {}", response);
		// }
		// return response;

	}
}
