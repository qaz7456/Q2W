package tw.com.util;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tw.com.heartbeat.clinet.serivce.HeartBeatService;
import tw.com.heartbeat.clinet.vo.HeartBeatClientVO;

public class Q2W {

	private static final Logger logger = LogManager.getLogger(Q2W.class);
	public static String FILE_XML_PATH = null;
	public static String CONVERT_XML_PATH = null;
	public static String HEART_BEAT_XML_FILE_PATH = null;

	private static Thread thread = new Thread() {
		@Override
		public void run() {
			String message = null;

			Document configDoc = XmlUtil.getDocument(FILE_XML_PATH);
			Element configRoot = configDoc.getDocumentElement();
			NodeList heartBeatClient = configRoot.getElementsByTagName("HeartBeatClient");

			NodeList clientInfo = heartBeatClient.item(0).getChildNodes();

			String beatID = null;
			String fileName = null;
			long timeSeries = 0;
			LocalDateTime localDateTime = LocalDateTime.now();

			for (int i = 0; i < clientInfo.getLength(); i++) {
				Node node = (Node) clientInfo.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					String value = node.getTextContent();

					beatID = nodeName.equals("beatID") ? value : beatID;
					fileName = nodeName.equals("fileName") ? value : fileName;
					timeSeries = nodeName.equals("timeSeries") ? Long.parseLong(value) : timeSeries;
				}
			}

			while (true) {
				try {
					HeartBeatClientVO heartBeatClientVO = new HeartBeatClientVO();

					heartBeatClientVO.setBeatID(beatID);
					heartBeatClientVO.setFileName(fileName);
					heartBeatClientVO.setLocalDateTime(localDateTime);
					heartBeatClientVO.setTimeSeries(timeSeries);

					HeartBeatService heartBeatService = new HeartBeatService(HEART_BEAT_XML_FILE_PATH);
					heartBeatService.setHeartBeatClientVO(heartBeatClientVO);

					heartBeatService.beat();

					message = RabbitMQ.Pull();

					if (message != null) {
						try {
							logger.debug("提取: {}", message);
							logger.debug("開始進行WebService前置動作");
							String result = WebService.execute(message);
							logger.debug("WebServic響應: {}", result);
							logger.debug("開始推送到Queue上");
							RabbitMQ.Push(result);
						} catch (Exception e) {
							logger.error(e.getMessage());
							RabbitMQ.ErrorPush(message);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				if (message == null) {
					try {
						logger.debug("暫無資料可提取");
						logger.debug("休息" + timeSeries + "毫秒");
						Thread.sleep(timeSeries);
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					}
				}
			}
		}
	};

	public static void main(String[] args) throws Exception {

		FILE_XML_PATH = args[0];
		CONVERT_XML_PATH = args[1];
		HEART_BEAT_XML_FILE_PATH = args[2];
		// FILE_XML_PATH = new File(FILE_XML_PATH).toURI().toString();

		// FILE_XML_PATH = "C:\\Users\\Ian\\Desktop\\Development\\q2w-config
		// -test.xml";
		// CONVERT_XML_PATH
		// ="C:\\Users\\Ian\\Desktop\\Development\\xmlconverter-config.xml";
		// HEART_BEAT_XML_FILE_PATH =
		// "C:\\Users\\Ian\\Desktop\\Kevin\\HeatBeatClinetBeans.xml";

		// FILE_XML_PATH = "D:\\JarManager\\jarXml\\test-q2w-config.xml";
		// CONVERT_XML_PATH =
		// "D:\\jarManager\\jarXml\\test-xmlconverter-config.xml";
		// HEART_BEAT_XML_FILE_PATH =
		// "D:\\jarManager\\jarXml\\test-HeatBeatClinetBeans.xml";

		thread.start();
	}
}
