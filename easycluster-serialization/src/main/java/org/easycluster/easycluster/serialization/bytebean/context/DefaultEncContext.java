
package org.easycluster.easycluster.serialization.bytebean.context;

import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: DefaultEncContext.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultEncContext extends AbstractCodecContext implements
		EncContext {

	private Object encObject;
	private EncContextFactory encContextFactory;

	public DefaultEncContext setCodecProvider(FieldCodecProvider codecProvider) {
		this.codecProvider = codecProvider;
		return this;
	}

	/**
	 * @param encClass
	 *            the encClass to set
	 */
	public DefaultEncContext setEncClass(Class<?> encClass) {
		this.targetType = encClass;
		return this;
	}

	/**
	 * @param encImpl
	 *            the encImpl to set
	 */
	public DefaultEncContext setFieldDesc(ByteFieldDesc desc) {
		this.fieldDesc = desc;
		return this;
	}

	/**
	 * @param numberCodec
	 *            the numberCodec to set
	 */
	public DefaultEncContext setNumberCodec(NumberCodec numberCodec) {
		this.numberCodec = numberCodec;
		return this;
	}

	/**
	 * @param encObject
	 *            the encObject to set
	 */
	public DefaultEncContext setEncObject(Object encObject) {
		this.encObject = encObject;
		return this;
	}

	/**
	 * @param encContextFactory
	 *            the encContextFactory to set
	 */
	public DefaultEncContext setEncContextFactory(
			EncContextFactory encContextFactory) {
		this.encContextFactory = encContextFactory;
		return this;
	}

	@Override
	public Object getEncObject() {
		return encObject;
	}

	@Override
	public Class<?> getEncClass() {
		return this.targetType;
	}

	@Override
	public EncContextFactory getEncContextFactory() {
		return encContextFactory;
	}

}
