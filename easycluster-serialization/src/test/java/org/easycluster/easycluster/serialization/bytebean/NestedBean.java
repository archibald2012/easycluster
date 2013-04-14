/*******************************************************************************
 * CopyRight (c) 2005-2011 TAOTAOSOU Co, Ltd. All rights reserved.
 * Filename:    NestedBean.java
 * Creator:     wangqi
 * Create-Date: 2011-4-27 下午03:47:21
 *******************************************************************************/
package org.easycluster.easycluster.serialization.bytebean;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.ByteBean;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: NestedBean.java 14 2012-01-10 11:54:14Z archie $
 */
public class NestedBean implements ByteBean {
	@ByteField(index = 0)
	private int intField;

	@ByteField(index = 1)
	private short shortField;

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
