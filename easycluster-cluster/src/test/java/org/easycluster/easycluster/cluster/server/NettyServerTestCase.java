package org.easycluster.easycluster.cluster.server;

import java.util.ArrayList;

import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NettyServerTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetHandler() {
		NetworkServer server = new NetworkServer("", "", "");
		ArrayList<MessageClosure<?, ?>> handlers = new ArrayList<MessageClosure<?, ?>>();
		handlers.add(new SampleMessageClosure());
		server.setHandlers(handlers);
	}

}
