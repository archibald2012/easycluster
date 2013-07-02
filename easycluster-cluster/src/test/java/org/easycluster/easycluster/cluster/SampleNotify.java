package org.easycluster.easycluster.cluster;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;
import org.easycluster.easycluster.serialization.kv.annotation.KeyValueAttribute;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.protocol.xip.XipNotify;
import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: SampleSignal.java 4 2012-01-10 11:51:54Z archie $
 */
@SignalCode(messageCode = 0x121)
@XmlRootElement(name = "node")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Node", propOrder = { "intField", "byteField", "stringField", "byteArrayField", "shortField", "longField", "nanoTime" })
public class SampleNotify extends AbstractXipSignal implements XipNotify, Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@ByteField(index = 0)
	@TLVAttribute(tag = 0)
	@KeyValueAttribute(key = "intField")
	@XmlElement(name = "intField", required = true)
	private int					intField;

	@ByteField(index = 1)
	@TLVAttribute(tag = 1)
	@KeyValueAttribute
	@XmlElement(name = "byteField", required = true)
	private byte				byteField;

	@ByteField(index = 2)
	@TLVAttribute(tag = 2)
	@KeyValueAttribute
	@XmlElement(name = "stringField", required = true)
	private String				stringField			= "";

	@ByteField(index = 3)
	@TLVAttribute(tag = 3)
	@KeyValueAttribute
	@XmlElement(name = "byteArrayField", required = true)
	private byte[]				byteArrayField		= new byte[0];

	@ByteField(index = 4)
	@TLVAttribute(tag = 4)
	@KeyValueAttribute
	@XmlElement(name = "shortField", required = true)
	private short				shortField;

	@ByteField(index = 5)
	@TLVAttribute(tag = 5)
	@KeyValueAttribute
	@XmlElement(name = "longField", required = true)
	private long				longField;

	@ByteField(index = 6)
	@TLVAttribute(tag = 6)
	@KeyValueAttribute
	@XmlElement(name = "nanoTime", required = true)
	private long				nanoTime			= System.nanoTime();

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
