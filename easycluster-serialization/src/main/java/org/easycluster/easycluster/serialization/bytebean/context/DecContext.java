
package org.easycluster.easycluster.serialization.bytebean.context;


/**
 * TODO
 * 
 * @author Archibald.Wang
 * @version $Id: DecContext.java 14 2012-01-10 11:54:14Z archie $
 */
public interface DecContext extends FieldCodecContext {
	Object getDecOwner();
	byte[] getDecBytes();
	Class<?> getDecClass();

	DecContextFactory getDecContextFactory();
}
