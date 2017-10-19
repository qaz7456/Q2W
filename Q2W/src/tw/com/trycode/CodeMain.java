package tw.com.trycode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

import tw.com.util.Q2W;
import tw.com.util.RabbitMQ;
import tw.com.util.WebService;
import tw.com.util.XMLConverter;

public class CodeMain {

	public static void main(String[] args) throws Exception {
		String xml = "<Request service=\"SALE_ORDER_STATUS_PUSH_SERVICE\" lang=\"zh-CN\"><Body><SaleOrderStatusRequest><CompanyCode>W8860571504</CompanyCode><SaleOrders><SaleOrder><WarehouseCode>886DCA</WarehouseCode><ErpOrder>20170803TW1</ErpOrder><WayBillNo>289081343391</WayBillNo><ShipmentId>OXMS201708030114140703</ShipmentId><Waves>EW886A17080300102</Waves><CartNum>1</CartNum><GridNum>0001</GridNum><Carrier>顺丰速运</Carrier><CarrierProduct>島内件(80CM0.5-1.5KG)</CarrierProduct><IsSplit>N</IsSplit><Steps><Step><EventTime>2017-08-0315:31:25</EventTime><EventAddress>WOM</EventAddress><Status>1400</Status><Note>您的订单已取消,取消原因：客户要求取消订单</Note></Step></Steps></SaleOrder></SaleOrders></SaleOrderStatusRequest></Body></Request>";
		String json = "{\"Request\":{\"_lang\":\"zh-CN\",\"_service\":\"SALE_ORDER_STATUS_PUSH_SERVICE\",\"Body\":{\"SaleOrderStatusRequest\":{\"SaleOrders\":{\"SaleOrder\":{\"ShipmentId\":\"OXMS201708030114140703\",\"Sender\":\"顺丰速运\",\"Steps\":{\"Step\":{\"Status\":1400,\"EventTime\":\"2017-08-0315:31:25\",\"Note\":\"您的订单已取消,取消原因：客户要求取消订单\",\"EventAddress\":\"WOM\"}},\"ErpNo\":\"20170803TW1\",\"CartNum\":1,\"WayBillNo\":289081343391,\"GridNum\":\"0001\",\"Product\":\"島内件(80CM0.5-1.5KG)\",\"IsSplit\":\"N\",\"WarehouseCode\":\"886DCA\",\"Waves\":\"EW886A17080300102\"}},\"Code\":\"W8860571504\"}}}}";

//		WebService.execute(xml);
//		RabbitMQ.Push(xml);
//		 new Q2W().start();
//		for (int i = 0; i < 5; i++) {
//			RabbitMQ.Push(xml);
//		}
	}
}