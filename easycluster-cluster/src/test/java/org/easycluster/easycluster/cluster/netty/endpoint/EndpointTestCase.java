package org.easycluster.easycluster.cluster.netty.endpoint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.netty.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.netty.serialization.SerializeType;
import org.easycluster.easycluster.cluster.netty.tcp.TcpClient;
import org.easycluster.easycluster.cluster.netty.tcp.TcpServer;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EndpointTestCase {

	private TcpServer	nettyNetworkServer;
	private TcpClient	nettyNetworkClient;

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
	}

	@Test
	public void test() throws Exception {

		final SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

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
		serverConfig.setEndpointListener(new EndpointListener() {

			@Override
			public void onCreate(Endpoint endpoint) {
				assertTrue(endpoint.isConnected());
				System.out.println(endpoint.getRemoteAddress());

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				endpoint.close();
			}

			@Override
			public void onStop(Endpoint endpoint) {
				assertFalse(endpoint.isConnected());
			}
		});

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
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setSerializationConfig(clientCodecConfig);
		nettyNetworkClient = new TcpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();
	}

}
