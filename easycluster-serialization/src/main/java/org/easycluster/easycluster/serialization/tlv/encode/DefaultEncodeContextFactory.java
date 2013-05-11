/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.easycluster.easycluster.core.SimpleCache;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.tlv.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.tlv.meta.TLVCodecUtils;
import org.easycluster.easycluster.serialization.tlv.meta.TLVFieldMetainfo;

public class DefaultEncodeContextFactory implements TLVEncodeContextFactory {

	private NumberCodec								numberCodec;
	private TLVEncoderRepository					encoderRepository;

	private SimpleCache<Class<?>, TLVFieldMetainfo>	fieldMetainfoCache	= new SimpleCache<Class<?>, TLVFieldMetainfo>();

	private SimpleCache<Class<?>, Int2TypeMetainfo>	typeMetainfoCache	= new SimpleCache<Class<?>, Int2TypeMetainfo>();

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
		final TLVFieldMetainfo fieldMetainfo = fieldMetainfoCache.get(type, new Callable<TLVFieldMetainfo>() {

			public TLVFieldMetainfo call() throws Exception {
				return TLVCodecUtils.createFieldMetainfo(type);
			}
		});

		final Int2TypeMetainfo typeMetainfo = typeMetainfoCache.get(type, new Callable<Int2TypeMetainfo>() {

			public Int2TypeMetainfo call() throws Exception {
				return TLVCodecUtils.createTypeMetainfo(type);
			}
		});

		return new TLVEncodeContext() {

			public TLVEncoderRepository getEncoderRepository() {
				return encoderRepository;
			}

			public TLVFieldMetainfo getFieldMetainfo() {
				return fieldMetainfo;
			}

			public NumberCodec getNumberCodec() {
				return numberCodec;
			}

			public Int2TypeMetainfo getTypeMetainfo() {
				return typeMetainfo;
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
