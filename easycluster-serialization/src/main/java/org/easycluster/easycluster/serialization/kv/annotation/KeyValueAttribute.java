/**
 * 
 */
package org.easycluster.easycluster.serialization.kv.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author archie
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyValueAttribute {
	String key() default "";

	String desc() default "";
	
	boolean nullable() default true;
}
