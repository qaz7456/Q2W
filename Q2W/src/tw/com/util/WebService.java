package tw.com.util;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import tw.com.bean.WebServiceBean;

public class WebService {

	private static final Logger logger = LogManager.getLogger(WebService.class);
	
	public static String execute(String message) throws Exception {
		logger.debug("WebService FILE_XML_PATH: {}", Q2W.FILE_XML_PATH);
		logger.debug("WebService CONVERT_XML_PATH: {}", Q2W.CONVERT_XML_PATH);
		ApplicationContext context = new ClassPathXmlApplicationContext(Q2W.FILE_XML_PATH);
		WebServiceBean webServiceBean = (WebServiceBean) context.getBean("webService");
		HttpEntity responseEntity = null;
		String response = null;

		String format = webServiceBean.getFormat();
		String type = webServiceBean.getType();
		String url = webServiceBean.getUrl();

		logger.debug("format: {}", format);
		logger.debug("type: {}", type);
		logger.debug("url: {}", url);
		
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
