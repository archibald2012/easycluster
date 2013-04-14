package org.easycluster.easycluster.serialization.kv.context;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: DefaultEncContext.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultEncContext implements EncContext {

  private Object            encObject;
  private Class<?>          targetType;
  private EncContextFactory encContextFactory;

  /**
   * @param encClass
   *          the encClass to set
   */
  public DefaultEncContext setEncClass(Class<?> encClass) {
    this.targetType = encClass;
    return this;
  }

  /**
   * @param encObject
   *          the encObject to set
   */
  public DefaultEncContext setEncObject(Object encObject) {
    this.encObject = encObject;
    return this;
  }

  /**
   * @param encContextFactory
   *          the encContextFactory to set
   */
  public DefaultEncContext setEncContextFactory(EncContextFactory encContextFactory) {
    this.encContextFactory = encContextFactory;
    return this;
  }

  @Override
  public Object getEncObject() {
    return encObject;
  }

  @Override
  public Class<?> getEncClass() {
    return this.targetType;
  }

  @Override
  public EncContextFactory getEncContextFactory() {
    return encContextFactory;
  }

}
