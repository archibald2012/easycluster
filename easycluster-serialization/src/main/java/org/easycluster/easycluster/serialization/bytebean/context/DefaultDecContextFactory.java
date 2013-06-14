
package org.easycluster.easycluster.serialization.bytebean.context;

import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;

/**
 * TODO
 * @author wangqi
 * @version $Id: DefaultDecContextFactory.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultDecContextFactory implements DecContextFactory {

	private	FieldCodecProvider	codecProvider;
	private NumberCodec numberCodec;
	
	public DecContext createDecContext(byte[] decBytes, Class<?> targetType,
			Object parent, ByteFieldDesc desc) {
		return new DefaultDecContext()
				.setCodecProvider(codecProvider)
				.setDecBytes(decBytes)
				.setDecClass(targetType)
				.setDecOwner(parent)
				.setNumberCodec(numberCodec)
				.setFieldDesc(desc)
				.setDecContextFactory(this);
	}

	public FieldCodecProvider getCodecProvider() {
		return codecProvider;
	}

	public void setCodecProvider(FieldCodecProvider codecProvider) {
		this.codecProvider = codecProvider;
	}

	public NumberCodec getNumberCodec() {
		return numberCodec;
	}

	public void setNumberCodec(NumberCodec numberCodec) {
		this.numberCodec = numberCodec;
	}

}
