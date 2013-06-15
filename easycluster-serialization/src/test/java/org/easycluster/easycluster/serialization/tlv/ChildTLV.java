/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;

public class ChildTLV {
	@TLVAttribute(tag = 100)
	private int		num;

	@TLVAttribute(tag = 101)
	private Integer	num2;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + num;
		result = prime * result + ((num2 == null) ? 0 : num2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ChildTLV other = (ChildTLV) obj;
		if (num != other.num)
			return false;
		if (num2 == null) {
			if (other.num2 != null)
				return false;
		} else if (!num2.equals(other.num2))
			return false;
		return true;
	}

}
