package org.easycluster.easycluster.cluster.netty.tcp;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.easycluster.easycluster.cluster.common.AverageTracker;
import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.common.RequestsTracker;
import org.easycluster.easycluster.cluster.netty.MessageContextHolder;
import org.easycluster.easycluster.cluster.netty.NetworkClientStatisticsMBean;
import org.easycluster.easycluster.core.KeyTransformer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientChannelHandler extends SimpleChannelHandler {

	private static final Logger		LOGGER					= LoggerFactory.getLogger(ClientChannelHandler.class);

	private MessageContextHolder	messageContextHolder	= null;
	private KeyTransformer			keyTransformer			= new KeyTransformer();

	private String					mbeanObjectName			= "org.easycluster:type=NetworkClientStatistics,service=%s";
	private AverageTracker			processingTime			= new AverageTracker(100);
	private RequestsTracker			finishedTracker			= new RequestsTracker();

	public ClientChannelHandler(String service, MessageContextHolder messageContextHolder) {
		this.messageContextHolder = messageContextHolder;
		this.finishedTracker.start();
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName measurementName = new ObjectName(String.format(mbeanObjectName, service));
			if (mbeanServer.isRegistered(measurementName)) {
				mbeanServer.unregisterMBean(measurementName);
			}
			StandardMBean networkStatisticsMBean = new StandardMBean(new NetworkClientStatisticsMBean() {
				public double getFinishedPerSecond() {
					return finishedTracker.getFinishedThroughput();
				}

				public double getAverageRequestProcessingTime() {
					// 返回毫秒
					return processingTime.getAverage() / 1000000;
				}
			}, NetworkClientStatisticsMBean.class);
			mbeanServer.registerMBean(networkStatisticsMBean, measurementName);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Registering with JMX server as MBean [" + measurementName + "]");
			}
		} catch (Exception e) {
			String message = "Unable to register MBeans with error " + e.getMessage();
			LOGGER.error(message, e);
		}
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Write requested message: {}", e.getMessage());
		}

		if (e.getMessage() instanceof MessageContext) {
			MessageContext requestContext = (MessageContext) e.getMessage();
			Object requestId = keyTransformer.transform(requestContext.getMessage());
			messageContextHolder.add(requestId, requestContext);
			}
		super.writeRequested(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Object message = e.getMessage();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Received message: {}", message);
		}
		finishedTracker.increaseFinished();
		Object requestId = keyTransformer.transform(message);
		MessageContext requestContext = messageContextHolder.remove(requestId, message);
		if (requestContext != null) {
			processingTime.add(System.nanoTime() - requestContext.getTimestamp());
			requestContext.getClosure().execute(message);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		LOGGER.info("Caught exception in network layer", e.getCause());
	}

	public void setKeyTransformer(KeyTransformer keyTransformer) {
		this.keyTransformer = keyTransformer;
	}

}
