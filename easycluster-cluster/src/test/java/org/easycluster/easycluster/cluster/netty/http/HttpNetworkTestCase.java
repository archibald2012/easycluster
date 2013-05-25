package org.easycluster.easycluster.cluster.netty.http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpNetworkTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSend() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		HttpNetworkServer server = new HttpNetworkServer("app", "test", "127.0.0.1:2181");
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.setPort(5000);
		server.setPartitionIds(new Integer[] { 0, 1 });
		HttpRequestDecoder httpRequestDecoder = new HttpRequestDecoder();
		httpRequestDecoder.setTypeMetaInfo(typeMetaInfo);
		server.setDecoder(httpRequestDecoder);
		server.setEncoder(new HttpResponseEncoder());
		server.start();

		HttpNetworkClient client = new HttpNetworkClient("app", "test", "127.0.0.1:2181", new RoundRobinLoadBalancerFactory());
		HttpResponseDecoder responseDecoder = new HttpResponseDecoder();
		responseDecoder.setTypeMetaInfo(typeMetaInfo);
		client.setDecoder(responseDecoder);
		client.setEncoder(new HttpRequestEncoder());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = client.sendMessage(request);

		System.out.println("Result: " + future.get(20000, TimeUnit.SECONDS));
	}
	
	@Test
	public void testSend_JSON() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		HttpNetworkServer server = new HttpNetworkServer("app", "test", "127.0.0.1:2181");
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.setPort(5000);
		server.setPartitionIds(new Integer[] { 0, 1 });
		HttpRequestDecoder httpRequestDecoder = new HttpRequestDecoder();
		httpRequestDecoder.setTypeMetaInfo(typeMetaInfo);
		server.setDecoder(httpRequestDecoder);
		server.setEncoder(new HttpResponseEncoder());
		server.start();

		HttpNetworkClient client = new HttpNetworkClient("app", "test", "127.0.0.1:2181", new RoundRobinLoadBalancerFactory());
		HttpResponseDecoder responseDecoder = new HttpResponseDecoder();
		responseDecoder.setTypeMetaInfo(typeMetaInfo);
		client.setDecoder(responseDecoder);
		client.setEncoder(new HttpRequestEncoder());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = client.sendMessage(request);

		System.out.println("Result: " + future.get(20000, TimeUnit.SECONDS));
	}

}
