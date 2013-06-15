/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv;

import java.util.ArrayList;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;

@TLVAttribute(tag = 10002000)
public class SampleTLV extends AbstractXipSignal {
	
	@TLVAttribute(tag = 1)
	private int					age;

	@TLVAttribute(tag = 2)
	private ArrayList<ChildTLV>	children;

	@TLVAttribute(tag = 3)
	private String				name;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public ArrayList<ChildTLV> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<ChildTLV> children) {
		this.children = children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + age;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		final SampleTLV other = (SampleTLV) obj;
		if (age != other.age)
			return false;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
