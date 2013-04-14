
package org.easycluster.easycluster.serialization.protocol.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: SignalCode.java 14 2012-01-10 11:54:14Z archie $
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SignalCode {
	int messageCode();
}
