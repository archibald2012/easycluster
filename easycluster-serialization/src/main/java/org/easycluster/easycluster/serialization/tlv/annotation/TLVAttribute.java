/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TLVAttribute {

	public abstract int tag();

	public abstract Class<?> type() default TLVAttribute.class;

	public abstract String charset() default "";

	public abstract String description() default "";

	public abstract int bytes() default -1;
}
