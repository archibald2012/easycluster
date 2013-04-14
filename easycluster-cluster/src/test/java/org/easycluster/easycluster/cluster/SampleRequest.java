package org.easycluster.easycluster.cluster;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.protocol.xip.XipRequest;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: SampleSignal.java 4 2012-01-10 11:51:54Z archie $
 */
@SignalCode(messageCode = 0x121)
public class SampleRequest extends AbstractXipSignal implements XipRequest {

	@ByteField(index = 0)
	private int			intField;

	@ByteField(index = 1)
	private byte		byteField;

	@ByteField(index = 2)
	private String	stringField			= "";

	@ByteField(index = 3)
	private byte[]	byteArrayField	= new byte[0];

	@ByteField(index = 4)
	private short		shortField;

	@ByteField(index = 7)
	private long		longField;

	public int getIntField() {
		return intField;
	}

	public void setIntField(int intField) {
		this.intField = intField;
	}

	public short getShortField() {
		return shortField;
	}

	public void setShortField(short shortField) {
		this.shortField = shortField;
	}

	public long getLongField() {
		return longField;
	}

	public void setLongField(long longField) {
		this.longField = longField;
	}

	public byte getByteField() {
		return byteField;
	}

	public void setByteField(byte byteField) {
		this.byteField = byteField;
	}

	public String getStringField() {
		return stringField;
	}

	public void setStringField(String stringField) {
		this.stringField = stringField;
	}

	public byte[] getByteArrayField() {
		return byteArrayField;
	}

	public void setByteArrayField(byte[] byteArrayField) {
		this.byteArrayField = byteArrayField;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other);
	}
}
