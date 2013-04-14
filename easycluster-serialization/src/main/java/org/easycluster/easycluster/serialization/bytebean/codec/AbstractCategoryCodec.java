
package org.easycluster.easycluster.serialization.bytebean.codec;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: AbstractCategoryCodec.java 14 2012-01-10 11:54:14Z archie $
 */
public abstract class AbstractCategoryCodec implements ByteFieldCodec {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldCodec#getFieldType
	 * ()
	 */
	@Override
	public Class<?>[] getFieldType() {
		return null;
	}

}
