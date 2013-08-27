package org.easycluster.easycluster.cluster.manager.event;

import org.easycluster.easycluster.cluster.common.XmlUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClusterEventTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		LogUpdateEvent event = new LogUpdateEvent();
		event.setNewLevel("WARNING");
		ClusterEvent clusterEvent = new ClusterEvent(event);

		String xmlString = XmlUtil.marshal(clusterEvent);
		System.out.println(xmlString);

		ClusterEvent assertobj = XmlUtil.unmarshal(xmlString, ClusterEvent.class);
		System.out.println(assertobj);

		LogUpdateEvent assertLogUpdateEvent = (LogUpdateEvent) assertobj.getEvent();
		Assert.assertEquals(event.getNewLevel(), assertLogUpdateEvent.getNewLevel());
		Assert.assertEquals(event.getComponentName(), assertLogUpdateEvent.getComponentName());
		Assert.assertEquals(event.getComponentType(), assertLogUpdateEvent.getComponentType());
		Assert.assertEquals(event.getHostName(), assertLogUpdateEvent.getHostName());
		Assert.assertEquals(event.getPid(), assertLogUpdateEvent.getPid());
		Assert.assertEquals(event.getServiceName(), assertLogUpdateEvent.getServiceName());
		Assert.assertEquals(event.getType(), assertLogUpdateEvent.getType());

		MetricsUpdateEvent metricsUpdateEvent = new MetricsUpdateEvent();
		metricsUpdateEvent.setFunctionName("test");
		metricsUpdateEvent.setFilter(true);
		clusterEvent = new ClusterEvent(metricsUpdateEvent);

		xmlString = XmlUtil.marshal(clusterEvent);
		System.out.println(xmlString);

		assertobj = XmlUtil.unmarshal(xmlString, ClusterEvent.class);
		System.out.println(assertobj);

		MetricsUpdateEvent assertMetricsUpdateEvent = (MetricsUpdateEvent) assertobj.getEvent();
		Assert.assertEquals(metricsUpdateEvent.getFunctionName(), assertMetricsUpdateEvent.getFunctionName());
		Assert.assertEquals(metricsUpdateEvent.isFilter(), assertMetricsUpdateEvent.isFilter());
		Assert.assertEquals(metricsUpdateEvent.getComponentName(), assertMetricsUpdateEvent.getComponentName());
		Assert.assertEquals(metricsUpdateEvent.getComponentType(), assertMetricsUpdateEvent.getComponentType());
		Assert.assertEquals(metricsUpdateEvent.getHostName(), assertMetricsUpdateEvent.getHostName());
		Assert.assertEquals(metricsUpdateEvent.getPid(), assertMetricsUpdateEvent.getPid());
		Assert.assertEquals(metricsUpdateEvent.getServiceName(), assertMetricsUpdateEvent.getServiceName());
		Assert.assertEquals(metricsUpdateEvent.getType(), assertMetricsUpdateEvent.getType());

		MessageFilterEvent requestUpdateEvent = new MessageFilterEvent();
		requestUpdateEvent.setMessageType("test");
		requestUpdateEvent.setFilter(true);
		clusterEvent = new ClusterEvent(requestUpdateEvent);

		xmlString = XmlUtil.marshal(clusterEvent);
		System.out.println(xmlString);

		assertobj = XmlUtil.unmarshal(xmlString, ClusterEvent.class);
		System.out.println(assertobj);

		MessageFilterEvent assertRequestUpdateEvent = (MessageFilterEvent) assertobj.getEvent();
		Assert.assertEquals(requestUpdateEvent.getMessageType(), assertRequestUpdateEvent.getMessageType());
		Assert.assertEquals(requestUpdateEvent.isFilter(), assertRequestUpdateEvent.isFilter());
		Assert.assertEquals(requestUpdateEvent.getComponentName(), assertRequestUpdateEvent.getComponentName());
		Assert.assertEquals(requestUpdateEvent.getComponentType(), assertRequestUpdateEvent.getComponentType());
		Assert.assertEquals(requestUpdateEvent.getHostName(), assertRequestUpdateEvent.getHostName());
		Assert.assertEquals(requestUpdateEvent.getPid(), assertRequestUpdateEvent.getPid());
		Assert.assertEquals(requestUpdateEvent.getServiceName(), assertRequestUpdateEvent.getServiceName());
		Assert.assertEquals(requestUpdateEvent.getType(), assertRequestUpdateEvent.getType());
	}

}
