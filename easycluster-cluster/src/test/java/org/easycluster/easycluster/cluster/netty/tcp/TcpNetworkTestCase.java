package org.easycluster.easycluster.cluster.netty.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.codec.SerializationConfig;
import org.easycluster.easycluster.cluster.netty.codec.SerializeType;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TcpNetworkTestCase {

	private TcpConnector	nettyNetworkClient;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = InvalidMessageException.class)
	public void testInvalidMessage() throws Exception {

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig codecConfig = new SerializationConfig();
		clientConfig.setSerializationConfig(codecConfig);

		nettyNetworkClient = new TcpConnector(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.start();
		nettyNetworkClient.sendMessage("teststring");
	}

	@Test
	public void testInvalidMessage_batch() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		serverConfig.setSerializationConfig(codecConfig);

		TcpAcceptor nettyNetworkServer = new TcpAcceptor(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpConnector nettyNetworkClient = new TcpConnector(clientConfig, new RoundRobinLoadBalancerFactory());
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

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_json() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setSerializationConfig(codecConfig);

		TcpAcceptor nettyNetworkServer = new TcpAcceptor(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpConnector nettyNetworkClient = new TcpConnector(clientConfig, new RoundRobinLoadBalancerFactory());
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

		System.out.println("Result: " + future.get(20, TimeUnit.SECONDS));
	}

	@Test
	public void testSend_binary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setDecodeBytesDebugEnabled(true);
		codecConfig.setEncodeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.BINARY);
		serverConfig.setSerializationConfig(codecConfig);

		TcpAcceptor nettyNetworkServer = new TcpAcceptor(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setDecodeBytesDebugEnabled(true);
		clientCodecConfig.setEncodeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.BINARY);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpConnector nettyNetworkClient = new TcpConnector(clientConfig, new RoundRobinLoadBalancerFactory());
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

		System.out.println("Result: " + future.get(1800, TimeUnit.SECONDS));
	}

	@Test
	public void testSend_batchBinary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		serverConfig.setSerializationConfig(codecConfig);

		TcpAcceptor nettyNetworkServer = new TcpAcceptor(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(1000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpConnector nettyNetworkClient = new TcpConnector(clientConfig, new RoundRobinLoadBalancerFactory());
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

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testSend_batchJson() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setSerializationConfig(codecConfig);

		TcpAcceptor nettyNetworkServer = new TcpAcceptor(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpConnector nettyNetworkClient = new TcpConnector(clientConfig, new RoundRobinLoadBalancerFactory());
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

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}
}
