/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TLVAttribute {

	int tag();

	Class<?> type() default TLVAttribute.class;

	String charset() default "";

	String description() default "";

	int bytes() default -1;
}
