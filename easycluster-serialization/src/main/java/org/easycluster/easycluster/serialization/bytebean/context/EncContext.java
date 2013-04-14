
package org.easycluster.easycluster.serialization.bytebean.context;


/**
 * TODO
 * 
 * @author Archibald.Wang
 * @version $Id: EncContext.java 14 2012-01-10 11:54:14Z archie $
 */
public interface EncContext extends FieldCodecContext {
	Object getEncObject();
	Class<?> getEncClass();

	EncContextFactory getEncContextFactory();
}
