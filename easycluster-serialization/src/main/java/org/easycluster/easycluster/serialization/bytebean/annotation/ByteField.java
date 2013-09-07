
package org.easycluster.easycluster.serialization.bytebean.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定义协议字段
 * 
 * @author wangqi
 * @version $Id: ByteField.java 14 2012-01-10 11:54:14Z archie $
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ByteField {

	int index();

	int bytes() default -1;

	String length() default "";

	String charset() default "UTF-16";

	int fixedLength() default -1;

	String description() default "";
}
