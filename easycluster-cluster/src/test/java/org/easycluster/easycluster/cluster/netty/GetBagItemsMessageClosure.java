package org.easycluster.easycluster.cluster.netty;

import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetBagItemsMessageClosure implements MessageClosure<GetBagItemsReq, GetBagItemsResp> {

	private static final Logger	logger	= LoggerFactory.getLogger(GetBagItemsMessageClosure.class);

	@Override
	public GetBagItemsResp execute(GetBagItemsReq input) {
		logger.debug("try echo:" + input);
		GetBagItemsResp response = new GetBagItemsResp();

		return response;
	}

}
