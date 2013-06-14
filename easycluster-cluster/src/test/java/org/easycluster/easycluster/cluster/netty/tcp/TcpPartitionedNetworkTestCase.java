package org.easycluster.easycluster.cluster.netty.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.IntegerConsistentHashPartitionedLoadBalancerFactory;
import org.easycluster.easycluster.cluster.netty.codec.SerializationConfig;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
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
	public void testBind() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("edu.hziee.common.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);
		
		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 1 });

		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
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
		clientCodecConfig.setDecodeBytesDebugEnabled(false);
		clientConfig.setSerializationConfig(clientCodecConfig);

		TcpPartitionedConnector<Integer> nettyNetworkClient = new TcpPartitionedConnector<Integer>(clientConfig,
				new IntegerConsistentHashPartitionedLoadBalancerFactory(1));
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();
		
		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");
		request.setByteArrayField(new byte[] { 127 });

		int partitionId = 1;
		Future<Object> future = nettyNetworkClient.sendMessage(partitionId, request);

		System.out.println("Result: " + future.get(20, TimeUnit.SECONDS));
	}

}
