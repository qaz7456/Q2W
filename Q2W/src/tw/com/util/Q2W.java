package tw.com.util;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.bean.ConnectionBean;
import tw.com.heartbeat.clinet.serivce.HeartBeatService;

public class Q2W {

	private static final Logger logger = LogManager.getLogger(Q2W.class);
	public static String FILE_XML_PATH = null;
	public static String CONVERT_XML_PATH= null;

	private static Thread thread = new Thread() {
		@Override
		public void run() {
			ApplicationContext context = new ClassPathXmlApplicationContext(FILE_XML_PATH);
			HeartBeatService service = (HeartBeatService) context.getBean("heartBeatService");

			String message = null;
			while (true) {
				try {
					message = RabbitMQ.Pull();

					logger.debug("提取: {}", message);
					if (message != null) {
						logger.debug("開始發送至WebService");
						message = WebService.execute(message);
						logger.debug("WebServic響應: {}", message);
						logger.debug("開始推送到Queue上");
						RabbitMQ.Push(message);
					}
					service.beat();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (message == null) {
					try {
						long breakTime = service.getHeartBeatClientVO().getTimeSeries();
						logger.debug("休息" + breakTime + "毫秒");
						Thread.sleep(breakTime);
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					}
				}
			}
		}
	};

	public static void main(String[] args) throws Exception {
		
		FILE_XML_PATH = args[0];
		FILE_XML_PATH = new File(FILE_XML_PATH).toURI().toString();
		CONVERT_XML_PATH = args[1];

		thread.start();
	}
}
