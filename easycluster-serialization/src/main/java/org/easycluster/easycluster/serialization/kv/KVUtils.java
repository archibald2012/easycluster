/**
 * 
 */
package org.easycluster.easycluster.serialization.kv;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.easycluster.easycluster.core.SimpleCache;
import org.easycluster.easycluster.serialization.kv.annotation.KeyValueAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author archie
 * 
 */
public class KVUtils {

  @SuppressWarnings("unused")
  private static final Logger                   logger        = LoggerFactory.getLogger(KVUtils.class);

  private static SimpleCache<Class<?>, Field[]> kvFieldsCache = new SimpleCache<Class<?>, Field[]>();

  public static Field[] getKVFieldsOf(final Class<?> kvType) {
    return kvFieldsCache.get(kvType, new Callable<Field[]>() {

      public Field[] call() throws Exception {
        return FieldUtil.getAnnotationFieldsOf(kvType, KeyValueAttribute.class);
      }
    });
  }
}
