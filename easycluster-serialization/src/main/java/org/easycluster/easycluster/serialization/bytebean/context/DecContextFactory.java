
package org.easycluster.easycluster.serialization.bytebean.context;

import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;

/**
 * TODO
 * 
 * @author Archibald.Wang
 * @version $Id: DecContextFactory.java 14 2012-01-10 11:54:14Z archie $
 */
public interface DecContextFactory {
	DecContext createDecContext(byte[] decBytes, Class<?> targetType, Object parent,
			ByteFieldDesc desc);
}
