package org.easycluster.easycluster.serialization.kv.context;

/**
 * TODO
 * 
 * @author Archibald.Wang
 * @version $Id: DecContextFactory.java 14 2012-01-10 11:54:14Z archie $
 */
public interface DecContextFactory {
  DecContext createDecContext(String decString, Class<?> targetType, Object parent);
}
