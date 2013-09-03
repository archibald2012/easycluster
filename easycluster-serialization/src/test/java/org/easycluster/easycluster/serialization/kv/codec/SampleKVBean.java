package org.easycluster.easycluster.serialization.kv.codec;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.ByteBean;
import org.easycluster.easycluster.serialization.kv.annotation.KeyValueAttribute;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: SampleBean.java 14 2012-01-10 11:54:14Z archie $
 */
public class SampleKVBean implements ByteBean {
	@KeyValueAttribute
	private String	hsman;

	@KeyValueAttribute(key = "hstype")
	private String	hstype;

	@KeyValueAttribute(key = "hswidth")
	private int		hswidth;

	@KeyValueAttribute(key = "hsheight")
	private long	hsheight;

	@KeyValueAttribute(key = "hsplat")
	private String	hsplat;

	@KeyValueAttribute(key = "version")
	private short[]	version	= new short[0];

	@KeyValueAttribute
	private byte	byteField;

	@KeyValueAttribute
	private short	shortField;

	@KeyValueAttribute
	private boolean	booleanField;

	private String	nonAnnotatedField;

	public String getHsman() {
		return hsman;
	}

	public void setHsman(String hsman) {
		this.hsman = hsman;
	}

	public String getHstype() {
		return hstype;
	}

	public void setHstype(String hstype) {
		this.hstype = hstype;
	}

	public int getHswidth() {
		return hswidth;
	}

	public void setHswidth(int hswidth) {
		this.hswidth = hswidth;
	}

	public long getHsheight() {
		return hsheight;
	}

	public void setHsheight(long hsheight) {
		this.hsheight = hsheight;
	}

	public String getHsplat() {
		return hsplat;
	}

	public void setHsplat(String hsplat) {
		this.hsplat = hsplat;
	}

	public short[] getVersion() {
		return version;
	}

	public void setVersion(short[] version) {
		this.version = version;
	}

	public byte getByteField() {
		return byteField;
	}

	public void setByteField(byte byteField) {
		this.byteField = byteField;
	}

	public short getShortField() {
		return shortField;
	}

	public void setShortField(short shortField) {
		this.shortField = shortField;
	}

	public boolean isBooleanField() {
		return booleanField;
	}

	public void setBooleanField(boolean booleanField) {
		this.booleanField = booleanField;
	}

	public String getNonAnnotatedField() {
		return nonAnnotatedField;
	}

	public void setNonAnnotatedField(String nonAnnotatedField) {
		this.nonAnnotatedField = nonAnnotatedField;
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
