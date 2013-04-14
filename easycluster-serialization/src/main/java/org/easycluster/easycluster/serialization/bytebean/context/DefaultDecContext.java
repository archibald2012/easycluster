
package org.easycluster.easycluster.serialization.bytebean.context;

import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: DefaultDecContext.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultDecContext extends AbstractCodecContext implements
		DecContext {

	private byte[] decBytes;
	private Object decOwner;
	private DecContextFactory decContextFactory;

	public DefaultDecContext setCodecProvider(FieldCodecProvider codecProvider) {
		this.codecProvider = codecProvider;
		return this;
	}

	/**
	 * @param decClass
	 *            the decClass to set
	 */
	public DefaultDecContext setDecClass(Class<?> decClass) {
		this.targetType = decClass;
		return this;
	}

	/**
	 * @param decBytes
	 *            the decBytes to set
	 */
	public DefaultDecContext setDecBytes(byte[] decBytes) {
		this.decBytes = decBytes;
		return this;
	}

	/**
	 * @param decImpl
	 *            the decImpl to set
	 */
	public DefaultDecContext setFieldDesc(ByteFieldDesc desc) {
		this.fieldDesc = desc;
		return this;
	}

	/**
	 * @param decOwner
	 *            the decOwner to set
	 */
	public DefaultDecContext setDecOwner(Object decOwner) {
		this.decOwner = decOwner;
		return this;
	}

	/**
	 * @param numberCodec
	 *            the numberCodec to set
	 */
	public DefaultDecContext setNumberCodec(NumberCodec numberCodec) {
		this.numberCodec = numberCodec;
		return this;
	}

	/**
	 * @param decContextFactory
	 *            the decContextFactory to set
	 */
	public DefaultDecContext setDecContextFactory(
			DecContextFactory decContextFactory) {
		this.decContextFactory = decContextFactory;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.context.DecContext#getDecOwner
	 * ()
	 */
	@Override
	public Object getDecOwner() {
		return decOwner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.context.DecContext#getDecBytes
	 * ()
	 */
	@Override
	public byte[] getDecBytes() {
		return decBytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.context.DecContext#getDecClass
	 * ()
	 */
	@Override
	public Class<?> getDecClass() {
		return this.targetType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taotaosou.common.serialization.bytebean.context.DecContext#
	 * getDecContextFactory()
	 */
	@Override
	public DecContextFactory getDecContextFactory() {
		return decContextFactory;
	}

}
