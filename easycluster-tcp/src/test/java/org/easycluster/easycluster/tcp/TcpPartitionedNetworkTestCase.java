package org.easycluster.easycluster.tcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.loadbalancer.IntegerRoundRobinPartitionedLoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.ResponseIterator;
import org.easycluster.easycluster.cluster.exception.InvalidNodeException;
import org.easycluster.easycluster.cluster.exception.NetworkShutdownException;
import org.easycluster.easycluster.cluster.exception.NoNodesAvailableException;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.serialization.SerializeType;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TcpPartitionedNetworkTestCase {

	private TcpServer						nettyNetworkServer;
	private TcpServer						nettyNetworkServer2;
	private TcpPartitionedClient<Integer>	nettyNetworkClient;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		if (nettyNetworkClient != null) {
			nettyNetworkClient.stop();
		}
		if (nettyNetworkServer != null) {
			nettyNetworkServer.stop();
		}
		if (nettyNetworkServer2 != null) {
			nettyNetworkServer2.stop();
		}
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
		serverConfig.setPartitions(new Integer[] { 1 });

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
		serverConfig2.setPartitions(new Integer[] { 2 });

		nettyNetworkServer2 = new TcpServer(serverConfig2);
		nettyNetworkServer2.setHandlers(handlers);
		nettyNetworkServer2.start();

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
		
		nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig, new IntegerRoundRobinPartitionedLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

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

	@Test
	public void testSendMessage_ManyPartition() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

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
		serverConfig2.setPartitions(new Integer[] { 2 });

		nettyNetworkServer2 = new TcpServer(serverConfig2);
		nettyNetworkServer2.setHandlers(handlers);
		nettyNetworkServer2.start();

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
		
		nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig, new IntegerRoundRobinPartitionedLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Set<Integer> partitions = new HashSet<Integer>();
		partitions.add(new Integer(1));
		partitions.add(new Integer(2));
		ResponseIterator ri = nettyNetworkClient.sendMessage(partitions, request);

		while (ri.hasNext()) {
			SampleResponse assertobj = (SampleResponse) ri.next(60L, TimeUnit.SECONDS);
			Assert.assertEquals(request.getIntField(), assertobj.getIntField());
			Assert.assertEquals(request.getShortField(), assertobj.getShortField());
			Assert.assertEquals(request.getLongField(), assertobj.getLongField());
			Assert.assertEquals(request.getByteField(), assertobj.getByteField());
			Assert.assertEquals(request.getStringField(), assertobj.getStringField());
		}

	}

	@Test
	public void testSendMessageToNode() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

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
		serverConfig2.setPartitions(new Integer[] { 1 });

		nettyNetworkServer2 = new TcpServer(serverConfig2);
		nettyNetworkServer2.setHandlers(handlers);
		nettyNetworkServer2.start();

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
		
		nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig, new IntegerRoundRobinPartitionedLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Node node = nettyNetworkClient.nextNode(new Integer(1));
		Future<Object> f = nettyNetworkClient.sendMessageToNode(request, node);

		SampleResponse assertobj = (SampleResponse) f.get(60L, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test(expected = NoNodesAvailableException.class)
	public void testSendMessage_NoNodeAvailable() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

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
		
		nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig, new IntegerRoundRobinPartitionedLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		nettyNetworkClient.sendMessage(new Integer(2), request);

	}

	@Test(expected = NetworkShutdownException.class)
	public void testSendMessage_NetworkShutdown() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

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
		
		nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig, new IntegerRoundRobinPartitionedLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		nettyNetworkClient.stop();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		nettyNetworkClient.sendMessage(new Integer(2), request);

	}

	@Test(expected = InvalidNodeException.class)
	public void testSendMessage_InvalidNode() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

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
		
		nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig, new IntegerRoundRobinPartitionedLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Node node = new Node("localhost", 1111, Arrays.asList(new Integer[] { 2 }));
		nettyNetworkClient.sendMessageToNode(request, node);

	}

	@Test
	public void testBatchSend_json() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.tcp");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.JSON);
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
		clientCodecConfig.setSerializeBytesDebugEnabled(false);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig, new IntegerRoundRobinPartitionedLoadBalancerFactory());
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

		int partitionId = 1;

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(nettyNetworkClient.sendMessage(partitionId, client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((SampleResponse) futures.get(i).get(60, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, client1Responses.size());

	}

}
