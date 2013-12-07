package org.easycluster.easycluster.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.ResponseIterator;
import org.easycluster.easycluster.cluster.common.SystemUtil;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.serialization.SerializeType;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.cluster.ssl.SSLConfig;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TcpNetworkTestCase {

	private TcpServer	nettyNetworkServer;
	private TcpServer	nettyNetworkServer2;
	private TcpServer	nettyNetworkServer3;
	private TcpClient	nettyNetworkClient;

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {
		if (nettyNetworkClient != null) {
			nettyNetworkClient.stop();
		}
		if (nettyNetworkServer != null) {
			nettyNetworkServer.stop();
		}
		if (nettyNetworkServer2 != null) {
			nettyNetworkServer2.stop();
		}
		if (nettyNetworkServer3 != null) {
			nettyNetworkServer3.stop();
		}
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
		clientConfig.setEncodeSerializeConfig(codecConfig);
		clientConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.start();
		nettyNetworkClient.sendMessage("teststring");

		nettyNetworkClient.stop();
	}

	@Test
	public void testInvalidMessage_batch() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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
	}

	@Test
	public void testSend_json() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
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
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test
	public void testBroadcast_json() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		ArrayList<MessageClosure<?, ?>> handlers = new ArrayList<MessageClosure<?, ?>>();
		handlers.add(new SampleMessageClosure());
		nettyNetworkServer.setHandlers(handlers);
		nettyNetworkServer.start();

		NetworkServerConfig serverConfig2 = new NetworkServerConfig();
		serverConfig2.setServiceGroup("app");
		serverConfig2.setService("test");
		serverConfig2.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig2.setPort(6001);
		serverConfig2.setEncodeSerializeConfig(codecConfig);
		serverConfig2.setDecodeSerializeConfig(codecConfig);
		nettyNetworkServer2 = new TcpServer(serverConfig2);
		nettyNetworkServer2.setHandlers(handlers);
		nettyNetworkServer2.start();

		NetworkServerConfig serverConfig3 = new NetworkServerConfig();
		serverConfig3.setServiceGroup("app");
		serverConfig3.setService("test");
		serverConfig3.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig3.setPort(6002);
		serverConfig3.setEncodeSerializeConfig(codecConfig);
		serverConfig3.setDecodeSerializeConfig(codecConfig);
		nettyNetworkServer3 = new TcpServer(serverConfig3);
		nettyNetworkServer3.setHandlers(handlers);
		nettyNetworkServer3.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 500;

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");

			request.setByteArrayField(new byte[] { 127 });

			ResponseIterator ri = nettyNetworkClient.broadcastMessage(request);

			while (ri.hasNext()) {
				SampleResponse assertobj = (SampleResponse) ri.next(60L, TimeUnit.SECONDS);
				Assert.assertEquals(request.getIntField(), assertobj.getIntField());
				Assert.assertEquals(request.getShortField(), assertobj.getShortField());
				Assert.assertEquals(request.getLongField(), assertobj.getLongField());
				Assert.assertEquals(request.getByteField(), assertobj.getByteField());
				Assert.assertEquals(request.getStringField(), assertobj.getStringField());
			}
		}
	}

	@Test
	public void testSend_java() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JAVA);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JAVA);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());
	}

	@Test
	public void testSend_binary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.BYTE_BEAN);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.BYTE_BEAN);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test
	public void testSend_tlv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.TLV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.TLV);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test
	public void testSend_kv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.KV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.KV);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);

		nettyNetworkServer.start();
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test
	public void testSend_batchBinary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(10000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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
			client1Responses.add((SampleResponse) futures.get(i).get(60, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSendTLS_batchBinary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		SSLConfig sslConfig = new SSLConfig();
		sslConfig.setKeyStore("/Users/wangqi/.serverkeystore");
		sslConfig.setKeyStorePassword("123456");
		sslConfig.setTrustStore("/Users/wangqi/.servertruststore");
		sslConfig.setTrustStorePassword("123456");
		serverConfig.setSslConfig(sslConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(10000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		SSLConfig clientSslConfig = new SSLConfig();
		clientSslConfig.setKeyStore("/Users/wangqi/.clientkeystore");
		clientSslConfig.setKeyStorePassword("123456");
		clientSslConfig.setTrustStore("/Users/wangqi/.clienttruststore");
		clientSslConfig.setTrustStorePassword("123456");
		clientConfig.setSslConfig(clientSslConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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
			client1Responses.add((SampleResponse) futures.get(i).get(60, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_batchJava() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.JAVA);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
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
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 500;

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
			client1Responses.add((SampleResponse) futures.get(i).get(600, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_batchJson() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 500;

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
			client1Responses.add((SampleResponse) futures.get(i).get(60, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, client1Responses.size());
		Assert.assertEquals(num, count.get());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_batchTlv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.TLV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
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
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 500;

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
			client1Responses.add((SampleResponse) futures.get(i).get(600, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_batchKv() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.KV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
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
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		int num = 500;

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
			client1Responses.add((SampleResponse) futures.get(i).get(60, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, count.get());
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSendTLS_json() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		SSLConfig sslConfig = new SSLConfig();
		sslConfig.setKeyStore("/Users/wangqi/.serverkeystore");
		sslConfig.setKeyStorePassword("123456");
		sslConfig.setTrustStore("/Users/wangqi/.servertruststore");
		sslConfig.setTrustStorePassword("123456");
		serverConfig.setSslConfig(sslConfig);

		nettyNetworkServer = new TcpServer(serverConfig);
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
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		SSLConfig clientSslConfig = new SSLConfig();
		clientSslConfig.setKeyStore("/Users/wangqi/.clientkeystore");
		clientSslConfig.setKeyStorePassword("123456");
		clientSslConfig.setTrustStore("/Users/wangqi/.clienttruststore");
		clientSslConfig.setTrustStorePassword("123456");
		clientConfig.setSslConfig(clientSslConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test(expected = TimeoutException.class)
	public void testSend_blackList() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		serverConfig.setBlacklist("127.0.0.1," + SystemUtil.getIpAddress());

		nettyNetworkServer = new TcpServer(serverConfig);
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
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

		future.get(1, TimeUnit.SECONDS);

	}
}
