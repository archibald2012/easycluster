package org.easycluster.easycluster.tcp;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;
import org.easycluster.easycluster.serialization.kv.annotation.KeyValueAttribute;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.protocol.xip.XipResponse;
import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;

@SignalCode(messageCode = 0x122)
public class SampleResponse extends AbstractXipSignal implements XipResponse, Serializable {

	private static final long	serialVersionUID	= 1L;

	@ByteField(index = 0)
	@TLVAttribute(tag = 0)
	@KeyValueAttribute
	private int					intField;

	@ByteField(index = 1)
	@TLVAttribute(tag = 1)
	@KeyValueAttribute
	private byte				byteField;

	@ByteField(index = 2)
	@TLVAttribute(tag = 2)
	@KeyValueAttribute
	private String				stringField			= "";

	@ByteField(index = 3)
	@TLVAttribute(tag = 3)
	@KeyValueAttribute
	private byte[]				byteArrayField		= new byte[0];

	@ByteField(index = 4)
	@TLVAttribute(tag = 4)
	@KeyValueAttribute
	private short				shortField;

	@ByteField(index = 5)
	@TLVAttribute(tag = 5)
	@KeyValueAttribute
	private long				longField;

	@ByteField(index = 6)
	@TLVAttribute(tag = 6)
	@KeyValueAttribute
	private long				nanoTime;

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

	public long getNanoTime() {
		return nanoTime;
	}

	public void setNanoTime(long nanoTime) {
		this.nanoTime = nanoTime;
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
