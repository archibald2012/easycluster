package org.easycluster.easycluster.cluster;

import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SampleMessageClosure implements MessageClosure<SampleRequest, SampleResponse> {

	private static final Logger	logger	= LoggerFactory.getLogger(SampleMessageClosure.class);

	@Override
	public SampleResponse execute(SampleRequest input) {
		logger.debug("try echo:" + input);
		SampleResponse response = new SampleResponse();
		response.setIntField(input.getIntField());
		response.setLongField(input.getLongField());
		response.setByteField(input.getByteField());
		response.setStringField(input.getStringField());
		response.setShortField(input.getShortField());
		response.setByteArrayField(input.getByteArrayField());
		response.setNanoTime(input.getNanoTime());
		return response;
	}

}
