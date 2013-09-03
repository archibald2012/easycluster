package org.easycluster.easycluster.serialization.protocol.xip;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangqi
 * 
 */
public class AbstractXipResponse extends AbstractXipSignal implements
		XipResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXipResponse.class);
	
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
			LOGGER.error("", e);
			return null;
		} catch (IllegalAccessException e) {
			LOGGER.error("", e);
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
