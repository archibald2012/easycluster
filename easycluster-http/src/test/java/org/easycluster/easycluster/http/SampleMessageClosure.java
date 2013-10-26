package org.easycluster.easycluster.http;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.easycluster.easycluster.cluster.server.MessageClosure;

public class SampleMessageClosure implements MessageClosure<SampleRequest, SampleResponse> {

	public static final Logger	LOGGER	= Logger.getLogger(SampleMessageClosure.class.getName());

	public SampleMessageClosure(){
		LOGGER.setLevel(Level.INFO);
	}
	
	@Override
	public SampleResponse execute(SampleRequest input) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, "try echo:" + input);
		}
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
