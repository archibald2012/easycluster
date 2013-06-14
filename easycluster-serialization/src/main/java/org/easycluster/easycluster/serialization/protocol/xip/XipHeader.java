package org.easycluster.easycluster.serialization.protocol.xip;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: XipHeader.java 23 2012-01-10 14:42:11Z archie $
 */
public class XipHeader {

	public static final int	HEADER_LENGTH	= 24;

	public static final int	XIP_REQUEST		= 1;
	public static final int	XIP_RESPONSE	= 2;
	public static final int	XIP_NOTIFY		= 3;

	@ByteField(index = 0)
	private int				length			= 0;

	@ByteField(index = 1, bytes = 1)
	private int				basicVer		= 1;

	@ByteField(index = 2)
	private long			sequence;

	@ByteField(index = 3)
	private long			clientId;

	@ByteField(index = 4, bytes = 1)
	private int				type			= 1;

	@ByteField(index = 5, bytes = 2)
	private int				reserved		= 0;

	public int getBasicVer() {
		return basicVer;
	}

	public void setBasicVer(int basicVer) {
		this.basicVer = basicVer;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public void setTypeForClass(Class<?> cls) {
		if (XipRequest.class.isAssignableFrom(cls)) {
			this.type = XIP_REQUEST;
		} else if (XipResponse.class.isAssignableFrom(cls)) {
			this.type = XIP_RESPONSE;
		} else if (XipNotify.class.isAssignableFrom(cls)) {
			this.type = XIP_NOTIFY;
		}
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
