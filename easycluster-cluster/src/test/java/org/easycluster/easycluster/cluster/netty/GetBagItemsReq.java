/**
 * 
 */
package org.easycluster.easycluster.cluster.netty;

import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipRequest;

@SignalCode(messageCode = 1080001)
public class GetBagItemsReq extends AbstractXipRequest {

	@ByteField(index = 0)
	private long	playerId;

	@ByteField(index = 1, bytes = 1)
	private int		type;

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
