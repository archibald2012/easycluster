/*******************************************************************************
 * CopyRight (c) 2005-2011 TAOTAOSOU Co, Ltd. All rights reserved.
 * Filename:    SampleBean.java
 * Creator:     wangqi
 * Create-Date: 2011-4-27 下午03:46:48
 *******************************************************************************/
package org.easycluster.easycluster.serialization.bytebean;

import java.util.ArrayList;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.ByteBean;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: SampleByteBean.java 178 2012-03-12 07:36:30Z archie $
 */
public class SampleByteBean implements ByteBean {
	@ByteField(index = 0)
	private int intField;

	@ByteField(index = 1)
	private byte byteField;

	@ByteField(index = 2)
	private String stringField = "";

	@ByteField(index = 3)
	private byte[] byteArrayField = new byte[0];

	@ByteField(index = 4)
	private short shortField;

	@ByteField(index = 5)
	// list字段不能为null，否则会报错
	private ArrayList<NestedBean> listField = new ArrayList<NestedBean>();

	@ByteField(index = 6)
	// 对象类型字段不能为null，否则会报错
	private NestedBean beanField = new NestedBean();

	@ByteField(index = 7)
	private boolean booleanField;

	@ByteField(index = 8)
	private long longField;

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

	public ArrayList<NestedBean> getListField() {
		return listField;
	}

	public void setListField(ArrayList<NestedBean> listField) {
		this.listField = listField;
	}

	public NestedBean getBeanField() {
		return beanField;
	}

	public void setBeanField(NestedBean beanField) {
		this.beanField = beanField;
	}

	public boolean isBooleanField() {
		return booleanField;
	}

	public void setBooleanField(boolean booleanField) {
		this.booleanField = booleanField;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other);
	}
}
