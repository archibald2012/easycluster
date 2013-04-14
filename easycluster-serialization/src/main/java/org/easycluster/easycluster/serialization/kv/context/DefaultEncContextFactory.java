package org.easycluster.easycluster.serialization.kv.context;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: DefaultEncContextFactory.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultEncContextFactory implements EncContextFactory {

  public EncContext createEncContext(Object encObject, Class<?> type) {
    return new DefaultEncContext().setEncClass(type).setEncObject(encObject).setEncContextFactory(this);
  }

}
