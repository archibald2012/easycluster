package org.easycluster.easycluster.cluster.server;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NetworkServerTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		SerializationConfig codecConfig = new SerializationConfig();
		serverConfig.setDecodeSerializeConfig(codecConfig);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		NetworkServer server = new NetworkServer(serverConfig);

		ArrayList<MessageClosure<?, ?>> handlers = new ArrayList<MessageClosure<?, ?>>();
		handlers.add(new SampleMessageClosure());
		server.setHandlers(handlers);

		assertTrue(server.messageClosureRegistry.messageRegistered(SampleRequest.class));
	}

}
