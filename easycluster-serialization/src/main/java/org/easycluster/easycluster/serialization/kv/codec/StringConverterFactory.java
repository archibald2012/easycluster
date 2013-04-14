/**
 * 
 */
package org.easycluster.easycluster.serialization.kv.codec;


/**
 * @author Administrator
 * 
 */
public interface StringConverterFactory {
  StringConverter getCodecOf(Class<?> clazz);
}
