package org.easycluster.easycluster.serialization.protocol.xip;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.protocol.xip.XipResponse;

/**
 * @author wangqi
 * 
 */
public class AbstractXipResponse extends AbstractXipSignal implements
		XipResponse {

	@ByteField(index = 0)
	private int errorCode;

	@ByteField(index = 1)
	private String errorMessage;

	public static <T extends AbstractXipResponse> T createRespForError(
			Class<T> clazz, int errorCode, String errorMessage) {
		T resp;
		try {
			resp = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}

		((AbstractXipResponse) resp).setErrorCode(errorCode);
		((AbstractXipResponse) resp).setErrorMessage(errorMessage);

		return resp;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
