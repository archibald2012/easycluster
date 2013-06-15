/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;

public class TestLongEncoderBean {

	@TLVAttribute(tag = 100, bytes = 4)
	private long	myLong;

	@TLVAttribute(tag = 101)
	private long	myLongNoAnnoBytes;

	/**
	 * @return the myLong
	 */
	public long getMyLong() {
		return myLong;
	}

	/**
	 * @param myLong
	 *            the myLong to set
	 */
	public void setMyLong(long myLong) {
		this.myLong = myLong;
	}

	/**
	 * @return the myLongNoAnnoBytes
	 */
	public long getMyLongNoAnnoBytes() {
		return myLongNoAnnoBytes;
	}

	/**
	 * @param myLongNoAnnoBytes
	 *            the myLongNoAnnoBytes to set
	 */
	public void setMyLongNoAnnoBytes(long myLongNoAnnoBytes) {
		this.myLongNoAnnoBytes = myLongNoAnnoBytes;
	}

}
