/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;

public class DefaultEncodeContextFactory implements TLVEncodeContextFactory {

	private NumberCodec								numberCodec;
	private TLVEncoderRepository					encoderRepository;

	public NumberCodec getNumberCodec() {
		return numberCodec;
	}

	public void setNumberCodec(NumberCodec numberCodec) {
		this.numberCodec = numberCodec;
	}

	public TLVEncoderRepository getEncoderRepository() {
		return encoderRepository;
	}

	public void setEncoderRepository(TLVEncoderRepository encoderRepository) {
		this.encoderRepository = encoderRepository;
	}

	public TLVEncodeContext createEncodeContext(final Class<?> type, final Field field) {

		return new TLVEncodeContext() {

			public TLVEncoderRepository getEncoderRepository() {
				return encoderRepository;
			}

			public NumberCodec getNumberCodec() {
				return numberCodec;
			}

			public Class<?> getValueType() {
				return type;
			}

			public Field getValueField() {
				return field;
			}
		};
	}

}
