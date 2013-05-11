package org.easycluster.easycluster.serialization.protocol.xip;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.protocol.xip.XipRequest;

/**
 * @author wangqi
 * 
 */
public class AbstractXipRequest extends AbstractXipSignal implements XipRequest {

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
