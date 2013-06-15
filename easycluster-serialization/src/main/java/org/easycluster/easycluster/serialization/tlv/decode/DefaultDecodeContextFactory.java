/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.easycluster.easycluster.core.SimpleCache;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.tlv.meta.TLVCodecUtils;
import org.easycluster.easycluster.serialization.tlv.meta.TLVFieldMetainfo;

public class DefaultDecodeContextFactory implements TLVDecodeContextFactory {

	private NumberCodec								numberCodec;
	private TLVDecoderRepository					decoderRepository;

	private SimpleCache<Class<?>, TLVFieldMetainfo>	fieldMetainfoCache	= new SimpleCache<Class<?>, TLVFieldMetainfo>();

	private SimpleCache<Class<?>, Int2TypeMetainfo>	typeMetainfoCache	= new SimpleCache<Class<?>, Int2TypeMetainfo>();

	public NumberCodec getNumberCodec() {
		return numberCodec;
	}

	public void setNumberCodec(NumberCodec numberCodec) {
		this.numberCodec = numberCodec;
	}

	public TLVDecoderRepository getDecoderRepository() {
		return decoderRepository;
	}

	public void setDecoderRepository(TLVDecoderRepository decoderRepository) {
		this.decoderRepository = decoderRepository;
	}

	public TLVDecodeContext createDecodeContext(final Class<?> type, final Field field) {
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

		return new TLVDecodeContext() {

			public TLVDecoderRepository getDecoderRepository() {
				return decoderRepository;
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
