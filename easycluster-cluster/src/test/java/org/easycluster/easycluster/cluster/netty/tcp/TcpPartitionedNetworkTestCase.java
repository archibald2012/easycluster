package org.easycluster.easycluster.cluster.netty.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.IntegerRoundRobinPartitionedLoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.ResponseIterator;
import org.easycluster.easycluster.cluster.netty.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.netty.serialization.SerializeType;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TcpPartitionedNetworkTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBroadcast_json() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

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

		NetworkServerConfig serverConfig2 = new NetworkServerConfig();
		serverConfig2.setServiceGroup("app");
		serverConfig2.setService("test");
		serverConfig2.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig2.setPort(6001);
		serverConfig2.setSerializationConfig(codecConfig);
		serverConfig2.setPartitions(new Integer[] { 2 });

		TcpServer nettyNetworkServer2 = new TcpServer(serverConfig2);
		nettyNetworkServer2.setHandlers(handlers);
		nettyNetworkServer2.start();

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

		TcpPartitionedClient<Integer> nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig,
				new IntegerRoundRobinPartitionedLoadBalancerFactory());
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
			SampleResponse assertobj = (SampleResponse) ri.next(1800L, TimeUnit.SECONDS);
			Assert.assertEquals(request.getIntField(), assertobj.getIntField());
			Assert.assertEquals(request.getShortField(), assertobj.getShortField());
			Assert.assertEquals(request.getLongField(), assertobj.getLongField());
			Assert.assertEquals(request.getByteField(), assertobj.getByteField());
			Assert.assertEquals(request.getStringField(), assertobj.getStringField());
		}

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

	@Test
	public void testBatchSend_json() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
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
		clientCodecConfig.setDecodeBytesDebugEnabled(false);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpPartitionedClient<Integer> nettyNetworkClient = new TcpPartitionedClient<Integer>(clientConfig,
				new IntegerRoundRobinPartitionedLoadBalancerFactory());
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
			client1Responses.add((SampleResponse) futures.get(i).get(1800, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, client1Responses.size());

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}

}
