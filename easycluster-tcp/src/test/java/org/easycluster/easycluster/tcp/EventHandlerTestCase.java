package org.easycluster.easycluster.tcp;

import java.util.logging.Level;

import javax.annotation.Resource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.easycluster.easycluster.cluster.common.XmlUtil;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.event.EventHandler;
import org.easycluster.easycluster.cluster.manager.event.LogUpdateEvent;
import org.easycluster.easycluster.cluster.manager.event.MessageFilterEvent;
import org.easycluster.easycluster.cluster.manager.event.MetricsUpdateEvent;
import org.easycluster.easycluster.cluster.server.NetworkServer;
import org.easymetrics.easymetrics.engine.MetricsEngine;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = { "applicationContext.xml" })
public class EventHandlerTestCase extends AbstractJUnit4SpringContextTests{

	@Resource
	private EventHandler		eventHandler;
	@Resource
	private MetricsEngine		metricsEngine;
	@Resource
	private NetworkServer		networkServer;

	@Test
	public void testNotService() {
		LogUpdateEvent event = new LogUpdateEvent("sampleMessageClosure", null, "WARNING");
		event.setServiceName("test_service");
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);
		eventHandler.handleClusterEvent(clusterEvent);
		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());
	}

	@Test
	public void testNotPid() {
		LogUpdateEvent event = new LogUpdateEvent("sampleMessageClosure", null, "WARNING");
		event.setPid("1234");
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);
		eventHandler.handleClusterEvent(clusterEvent);
		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());
	}

	@Test
	public void testNotHost() {
		LogUpdateEvent event = new LogUpdateEvent("sampleMessageClosure", null, "WARNING");
		event.setHostName("localhost");
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);
		eventHandler.handleClusterEvent(clusterEvent);
		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());
	}

	@Test
	public void testLogUpdateEvent() {
		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());

		LogUpdateEvent event = new LogUpdateEvent("sampleMessageClosure", null, "WARNING");
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertEquals(Level.WARNING, SampleMessageClosure.LOGGER.getLevel());

		event = new LogUpdateEvent("", null, "INFO");
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());

		event = new LogUpdateEvent("", SampleMessageClosure.class, "WARNING");
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertEquals(Level.WARNING, SampleMessageClosure.LOGGER.getLevel());
	}

	@Test
	public void testMetricsUpdateEvent() {

		MetricsUpdateEvent event = new MetricsUpdateEvent("sampleMessageClosure", null, "execute", true);
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertTrue(metricsEngine.isFilter("SampleMessageClosure", "execute"));

		event = new MetricsUpdateEvent("sampleMessageClosure", null, "execute", false);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertFalse(metricsEngine.isFilter("SampleMessageClosure", "execute"));

		event = new MetricsUpdateEvent("", SampleMessageClosure.class, "execute", false);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertFalse(metricsEngine.isFilter("SampleMessageClosure", "execute"));

		event = new MetricsUpdateEvent("", SampleMessageClosure.class, "execute", true);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertTrue(metricsEngine.isFilter("SampleMessageClosure", "execute"));

		event = new MetricsUpdateEvent("", null, "execute", true);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertTrue(metricsEngine.isFilter("SampleMessageClosure", "execute"));

		event = new MetricsUpdateEvent("", null, "execute", false);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertFalse(metricsEngine.isFilter("SampleMessageClosure", "execute"));
	}

	@Test
	public void testMessageFilterEvent() {

		MessageFilterEvent event = new MessageFilterEvent("tcpServer", null, SampleRequest.class, true);
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertFalse(networkServer.getMessageClosureRegistry().messageRegistered(SampleRequest.class));

		event = new MessageFilterEvent("tcpServer", null, SampleRequest.class, false);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertTrue(networkServer.getMessageClosureRegistry().messageRegistered(SampleRequest.class));

		event = new MessageFilterEvent("", NetworkServer.class, SampleRequest.class, true);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertFalse(networkServer.getMessageClosureRegistry().messageRegistered(SampleRequest.class));

		event = new MessageFilterEvent("", NetworkServer.class, SampleRequest.class, false);
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertTrue(networkServer.getMessageClosureRegistry().messageRegistered(SampleRequest.class));
	}

	@Test
	public void testRegisterEventObserver() throws Exception {

		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());

		LogUpdateEvent event = new LogUpdateEvent("sampleMessageClosure", null, "WARNING");
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertEquals(Level.WARNING, SampleMessageClosure.LOGGER.getLevel());

		final LogUpdateEvent event2 = new LogUpdateEvent("logUpdateHandlerExtension", null, "INFO");
		clusterEvent.setEvent(event2);

		eventHandler.handleClusterEvent(clusterEvent);

		Assert.assertEquals(Level.WARNING, SampleMessageClosure.LOGGER.getLevel());

	}

	@Test
	public void testZooKeeperRegisterEventOberver() throws Exception {
		LogUpdateEvent event;
		ClusterEvent clusterEvent;
		String zooKeeperConnectString = "127.0.0.1:2181";
		int sessionTimeout = 30000;
		String path = "/clusters/app/test/event";

		event = new LogUpdateEvent("logUpdateHandlerExtension", null, "WARNING");
		clusterEvent = new ClusterEvent(event);

		String eventData = XmlUtil.marshal(clusterEvent);

		ZooKeeper zooKeeper = new ZooKeeper(zooKeeperConnectString, sessionTimeout, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println("received event " + ToStringBuilder.reflectionToString(event));
			}
		});

		Stat stat = new Stat();
		zooKeeper.getData(path, false, stat);
		int oldVersion = stat.getVersion();

		zooKeeper.setData(path, eventData.getBytes("UTF-8"), oldVersion);

		Thread.sleep(1000);

		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());
	}

	@Test
	public void testZooKeeperLogUpdate() throws Exception {

		Assert.assertEquals(Level.INFO, SampleMessageClosure.LOGGER.getLevel());

		String zooKeeperConnectString = "127.0.0.1:2181";
		int sessionTimeout = 30000;
		String path = "/clusters/app/test/event";

		LogUpdateEvent event = new LogUpdateEvent("sampleMessageClosure", null, "WARNING");
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);

		String eventData = XmlUtil.marshal(clusterEvent);

		ZooKeeper zooKeeper = new ZooKeeper(zooKeeperConnectString, sessionTimeout, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println("received event " + ToStringBuilder.reflectionToString(event));
			}
		});

		Stat stat = new Stat();
		zooKeeper.getData(path, false, stat);
		int oldVersion = stat.getVersion();

		zooKeeper.setData(path, eventData.getBytes("UTF-8"), oldVersion);

		byte[] array = zooKeeper.getData(path, false, stat);

		Assert.assertEquals(oldVersion + 1, stat.getVersion());
		Assert.assertEquals(eventData, new String(array, "UTF-8"));

		Thread.sleep(1000);

		Assert.assertEquals(Level.WARNING, SampleMessageClosure.LOGGER.getLevel());

	}

	@Test
	public void testZooKeeperMetricsUpdate() throws Exception {

		String zooKeeperConnectString = "127.0.0.1:2181";
		int sessionTimeout = 30000;
		String path = "/clusters/app/test/event";

		MetricsUpdateEvent event = new MetricsUpdateEvent("sampleMessageClosure", null, "execute", true);
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);

		String eventData = XmlUtil.marshal(clusterEvent);

		ZooKeeper zooKeeper = new ZooKeeper(zooKeeperConnectString, sessionTimeout, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println("received event " + ToStringBuilder.reflectionToString(event));
			}
		});

		Stat stat = new Stat();
		zooKeeper.getData(path, false, stat);
		int oldVersion = stat.getVersion();

		zooKeeper.setData(path, eventData.getBytes("UTF-8"), oldVersion);

		byte[] array = zooKeeper.getData(path, false, stat);

		Assert.assertEquals(eventData, new String(array, "UTF-8"));

		Thread.sleep(1000);

		Assert.assertTrue(metricsEngine.isFilter("SampleMessageClosure", "execute"));

		event = new MetricsUpdateEvent("", SampleMessageClosure.class, "execute", false);
		clusterEvent.setEvent(event);

		eventData = XmlUtil.marshal(clusterEvent);

		zooKeeper.setData(path, eventData.getBytes("UTF-8"), stat.getVersion());

		array = zooKeeper.getData(path, false, stat);

		Assert.assertEquals(eventData, new String(array, "UTF-8"));

		Thread.sleep(1000);

		Assert.assertFalse(metricsEngine.isFilter("SampleMessageClosure", "execute"));
	}

	@Test
	public void testZooKeeperMessageFilter() throws Exception {

		String zooKeeperConnectString = "127.0.0.1:2181";
		int sessionTimeout = 30000;
		String path = "/clusters/app/test/event";

		MessageFilterEvent event = new MessageFilterEvent("tcpServer", null, SampleRequest.class, false);
		ClusterEvent clusterEvent = new ClusterEvent();
		clusterEvent.setEvent(event);

		String eventData = XmlUtil.marshal(clusterEvent);

		ZooKeeper zooKeeper = new ZooKeeper(zooKeeperConnectString, sessionTimeout, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println("received event " + ToStringBuilder.reflectionToString(event));
			}
		});

		Stat stat = new Stat();
		zooKeeper.getData(path, false, stat);
		int oldVersion = stat.getVersion();

		zooKeeper.setData(path, eventData.getBytes("UTF-8"), oldVersion);

		byte[] array = zooKeeper.getData(path, false, stat);

		Assert.assertEquals(eventData, new String(array, "UTF-8"));

		Thread.sleep(1000);

		Assert.assertTrue(networkServer.getMessageClosureRegistry().messageRegistered(SampleRequest.class));

		event = new MessageFilterEvent("tcpServer", null, SampleRequest.class, true);
		clusterEvent.setEvent(event);

		eventData = XmlUtil.marshal(clusterEvent);

		zooKeeper.setData(path, eventData.getBytes("UTF-8"), stat.getVersion());

		array = zooKeeper.getData(path, false, stat);

		Assert.assertEquals(eventData, new String(array, "UTF-8"));

		Thread.sleep(1000);

		Assert.assertFalse(networkServer.getMessageClosureRegistry().messageRegistered(SampleRequest.class));

	}

}
