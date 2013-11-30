package org.easycluster.easycluster.http;

import org.easycluster.easycluster.cluster.server.MessageClosure;

public class SampleMessageClosure implements MessageClosure<SampleRequest, SampleResponse> {

	@Override
	public SampleResponse execute(SampleRequest input) {

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
