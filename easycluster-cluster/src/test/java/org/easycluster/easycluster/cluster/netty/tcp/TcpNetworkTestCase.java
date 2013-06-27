package org.easycluster.easycluster.cluster.netty.tcp;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.netty.serialization.SerializeType;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TcpNetworkTestCase {

	private TcpClient				nettyNetworkClient;

	private JMXConnectorServer		cs;
	private JMXConnector			cc;
	private MBeanServerConnection	mbsc;

	@Before
	public void setUp() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://");
		cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
		cs.start();

		JMXServiceURL addr = cs.getAddress();

		// Now make a connector client using the server's address
		cc = JMXConnectorFactory.connect(addr);
		mbsc = cc.getMBeanServerConnection();

		// HtmlAdaptorServer html = new HtmlAdaptorServer();
		// ObjectName html_name = new ObjectName("Adaptor:name=html,port=8082");
		// mbs.registerMBean(html, html_name);
		//
		// html.start();
	}

	@After
	public void tearDown() throws Exception {
		cc.close();
		cs.stop();
	}

	@Test
	public void testLong() {
		long test = Long.MAX_VALUE;
		System.out.println(test);
		for (int i = 0; i < 10; i++) {
			System.out.println(test++);
		}
	}

	@Test(expected = InvalidMessageException.class)
	public void testInvalidMessage() throws Exception {

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig codecConfig = new SerializationConfig();
		clientConfig.setSerializationConfig(codecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.start();
		nettyNetworkClient.sendMessage("teststring");

		nettyNetworkClient.stop();
	}

	@Test
	public void testInvalidMessage_batch() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 1000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setByteArrayField(new byte[] { 127 });

			client1Requests.add(request);
		}

		long startTime = System.nanoTime();
		for (int i = 0; i < num; i++) {
			nettyNetworkClient.sendMessage(client1Requests.get(i));
		}
		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		ObjectName objectName2 = new ObjectName("org.easycluster:type=NetworkServerStatistics,service=test");

		System.out.println("Channels: " + mbsc.getAttribute(objectName2, "Channels"));
		System.out.println("RequestsPerSecond: " + mbsc.getAttribute(objectName2, "RequestsPerSecond"));
		Assert.assertEquals(1000, ((Long) mbsc.getAttribute(objectName2, "RequestCount")).intValue());
		System.out.println("FinishedPerSecond: " + mbsc.getAttribute(objectName2, "FinishedPerSecond"));
		Assert.assertEquals(0, ((Long) mbsc.getAttribute(objectName2, "FinishedCount")).intValue());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_json() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		ArrayList<MessageClosure<?, ?>> handlers = new ArrayList<MessageClosure<?, ?>>();
		handlers.add(new SampleMessageClosure());
		nettyNetworkServer.setHandlers(handlers);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(1800, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_java() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JAVA);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JAVA);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(1800, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_binary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.BYTE_BEAN);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.BYTE_BEAN);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(1800, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_tlv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.TLV);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.TLV);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(1800, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	// @Test
	public void testSend_xml() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.XML);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.XML);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(1800, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_kv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.KV);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.KV);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(1800, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_batchBinary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(10000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 50000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setByteArrayField(new byte[] { 127 });
			request.setClient(UUID.randomUUID().getMostSignificantBits());

			client1Requests.add(request);
		}

		final AtomicInteger count = new AtomicInteger();
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new MessageClosure<SampleRequest, SampleResponse>() {

			@Override
			public SampleResponse execute(SampleRequest input) {
				count.incrementAndGet();
				SampleResponse response = new SampleResponse();

				return response;
			}
		});

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(nettyNetworkClient.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((SampleResponse) futures.get(i).get(1800, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		ObjectName objectName = new ObjectName("org.easycluster:type=ThreadPoolMessageExecutor,name=threadpool-message-executor");
		System.out.println("RequestCount:" + mbsc.getAttribute(objectName, "RequestCount"));
		System.out.println("AverageProcessingTime:" + mbsc.getAttribute(objectName, "AverageProcessingTime") + " ms");
		System.out.println("AverageWaitTime:" + mbsc.getAttribute(objectName, "AverageWaitTime") + " ms");
		System.out.println("QueueSize:" + mbsc.getAttribute(objectName, "QueueSize"));

		ObjectName objectName2 = new ObjectName("org.easycluster:type=NetworkServerStatistics,service=test");
		System.out.println("Channels: " + mbsc.getAttribute(objectName2, "Channels"));
		System.out.println("RequestsPerSecond: " + mbsc.getAttribute(objectName2, "RequestsPerSecond"));
		Assert.assertEquals(50000, ((Long) mbsc.getAttribute(objectName2, "RequestCount")).intValue());
		System.out.println("FinishedPerSecond: " + mbsc.getAttribute(objectName2, "FinishedPerSecond"));
		Assert.assertEquals(50000, ((Long) mbsc.getAttribute(objectName2, "FinishedCount")).intValue());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_batchJava() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.JAVA);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(1000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.JAVA);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 5000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setByteArrayField(new byte[] { 127 });
			request.setClient(UUID.randomUUID().getMostSignificantBits());

			client1Requests.add(request);
		}

		final AtomicInteger count = new AtomicInteger();
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new MessageClosure<SampleRequest, SampleResponse>() {

			@Override
			public SampleResponse execute(SampleRequest input) {
				count.incrementAndGet();
				SampleResponse response = new SampleResponse();

				return response;
			}
		});

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(nettyNetworkClient.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((SampleResponse) futures.get(i).get(1800, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		ObjectName objectName = new ObjectName("org.easycluster:type=ThreadPoolMessageExecutor,name=threadpool-message-executor");
		System.out.println("RequestCount:" + mbsc.getAttribute(objectName, "RequestCount"));
		System.out.println("AverageProcessingTime:" + mbsc.getAttribute(objectName, "AverageProcessingTime") + " ms");
		System.out.println("AverageWaitTime:" + mbsc.getAttribute(objectName, "AverageWaitTime") + " ms");
		System.out.println("QueueSize:" + mbsc.getAttribute(objectName, "QueueSize"));

		ObjectName objectName2 = new ObjectName("org.easycluster:type=NetworkServerStatistics,service=test");
		System.out.println("Channels: " + mbsc.getAttribute(objectName2, "Channels"));
		System.out.println("RequestsPerSecond: " + mbsc.getAttribute(objectName2, "RequestsPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "RequestCount")).intValue());
		System.out.println("FinishedPerSecond: " + mbsc.getAttribute(objectName2, "FinishedPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "FinishedCount")).intValue());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_batchJson() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 5000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setByteArrayField(new byte[] { 127 });
			request.setClient(UUID.randomUUID().getMostSignificantBits());

			client1Requests.add(request);
		}

		final AtomicInteger count = new AtomicInteger();
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new MessageClosure<SampleRequest, SampleResponse>() {

			@Override
			public SampleResponse execute(SampleRequest input) {
				System.out.println("received : " + count.incrementAndGet());
				SampleResponse response = new SampleResponse();

				return response;
			}
		});

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(nettyNetworkClient.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((SampleResponse) futures.get(i).get(1800, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, client1Responses.size());
		Assert.assertEquals(num, count.get());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		ObjectName objectName = new ObjectName("org.easycluster:type=ThreadPoolMessageExecutor,name=threadpool-message-executor");
		System.out.println("RequestCount:" + mbsc.getAttribute(objectName, "RequestCount"));
		System.out.println("AverageProcessingTime:" + mbsc.getAttribute(objectName, "AverageProcessingTime") + " ms");
		System.out.println("AverageWaitTime:" + mbsc.getAttribute(objectName, "AverageWaitTime") + " ms");
		System.out.println("QueueSize:" + mbsc.getAttribute(objectName, "QueueSize"));

		ObjectName objectName2 = new ObjectName("org.easycluster:type=NetworkServerStatistics,service=test");
		System.out.println("Channels: " + mbsc.getAttribute(objectName2, "Channels"));
		System.out.println("RequestsPerSecond: " + mbsc.getAttribute(objectName2, "RequestsPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "RequestCount")).intValue());
		System.out.println("FinishedPerSecond: " + mbsc.getAttribute(objectName2, "FinishedPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "FinishedCount")).intValue());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_batchTlv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.TLV);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(1000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.TLV);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 5000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setByteArrayField(new byte[] { 127 });
			request.setClient(UUID.randomUUID().getMostSignificantBits());

			client1Requests.add(request);
		}

		final AtomicInteger count = new AtomicInteger();
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new MessageClosure<SampleRequest, SampleResponse>() {

			@Override
			public SampleResponse execute(SampleRequest input) {
				count.incrementAndGet();
				SampleResponse response = new SampleResponse();

				return response;
			}
		});

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(nettyNetworkClient.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((SampleResponse) futures.get(i).get(1800, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		ObjectName objectName = new ObjectName("org.easycluster:type=ThreadPoolMessageExecutor,name=threadpool-message-executor");
		System.out.println("RequestCount:" + mbsc.getAttribute(objectName, "RequestCount"));
		System.out.println("AverageProcessingTime:" + mbsc.getAttribute(objectName, "AverageProcessingTime") + " ms");
		System.out.println("AverageWaitTime:" + mbsc.getAttribute(objectName, "AverageWaitTime") + " ms");
		System.out.println("QueueSize:" + mbsc.getAttribute(objectName, "QueueSize"));

		ObjectName objectName2 = new ObjectName("org.easycluster:type=NetworkServerStatistics,service=test");
		System.out.println("Channels: " + mbsc.getAttribute(objectName2, "Channels"));
		System.out.println("RequestsPerSecond: " + mbsc.getAttribute(objectName2, "RequestsPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "RequestCount")).intValue());
		System.out.println("FinishedPerSecond: " + mbsc.getAttribute(objectName2, "FinishedPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "FinishedCount")).intValue());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_batchKv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.KV);
		serverConfig.setSerializationConfig(codecConfig);

		TcpServer nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(1000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.KV);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpClient nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 5000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setByteArrayField(new byte[] { 127 });
			request.setClient(UUID.randomUUID().getMostSignificantBits());

			client1Requests.add(request);
		}

		final AtomicInteger count = new AtomicInteger();
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new MessageClosure<SampleRequest, SampleResponse>() {

			@Override
			public SampleResponse execute(SampleRequest input) {
				count.incrementAndGet();
				SampleResponse response = new SampleResponse();

				return response;
			}
		});

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(nettyNetworkClient.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((SampleResponse) futures.get(i).get(1800, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		ObjectName objectName = new ObjectName("org.easycluster:type=ThreadPoolMessageExecutor,name=threadpool-message-executor");
		System.out.println("RequestCount:" + mbsc.getAttribute(objectName, "RequestCount"));
		System.out.println("AverageProcessingTime:" + mbsc.getAttribute(objectName, "AverageProcessingTime") + " ms");
		System.out.println("AverageWaitTime:" + mbsc.getAttribute(objectName, "AverageWaitTime") + " ms");
		System.out.println("QueueSize:" + mbsc.getAttribute(objectName, "QueueSize"));

		ObjectName objectName2 = new ObjectName("org.easycluster:type=NetworkServerStatistics,service=test");
		System.out.println("Channels: " + mbsc.getAttribute(objectName2, "Channels"));
		System.out.println("RequestsPerSecond: " + mbsc.getAttribute(objectName2, "RequestsPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "RequestCount")).intValue());
		System.out.println("FinishedPerSecond: " + mbsc.getAttribute(objectName2, "FinishedPerSecond"));
		Assert.assertEquals(5000, ((Long) mbsc.getAttribute(objectName2, "FinishedCount")).intValue());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();

	}
}
