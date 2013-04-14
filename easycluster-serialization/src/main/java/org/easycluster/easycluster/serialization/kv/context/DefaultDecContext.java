package org.easycluster.easycluster.serialization.kv.context;

import org.easycluster.easycluster.serialization.kv.codec.StringConverter;
import org.easycluster.easycluster.serialization.kv.codec.StringConverterFactory;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: DefaultDecContext.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultDecContext implements DecContext {

  private String                 decString;
  private Object                 decOwner;
  private Class<?>               targetType;

  private StringConverterFactory stringConverterFactory;

  private DecContextFactory      decContextFactory;

  /**
   * @param decClass
   *          the decClass to set
   */
  public DefaultDecContext setDecClass(Class<?> decClass) {
    this.targetType = decClass;
    return this;
  }

  /**
   * @param decString
   *          the decString to set
   */
  public DefaultDecContext setDecString(String decString) {
    this.decString = decString;
    return this;
  }

  /**
   * @param decOwner
   *          the decOwner to set
   */
  public DefaultDecContext setDecOwner(Object decOwner) {
    this.decOwner = decOwner;
    return this;
  }

  /**
   * 
   * @param stringConverterFactory
   *          the stringConverterFactory to set
   */
  public DefaultDecContext setStringConverterFactory(StringConverterFactory stringConverterFactory) {
    this.stringConverterFactory = stringConverterFactory;
    return this;
  }

  /**
   * @param decContextFactory
   *          the decContextFactory to set
   */
  public DefaultDecContext setDecContextFactory(DecContextFactory decContextFactory) {
    this.decContextFactory = decContextFactory;
    return this;
  }

  @Override
  public Object getDecOwner() {
    return decOwner;
  }

  @Override
  public String getDecString() {
    return decString;
  }

  @Override
  public Class<?> getDecClass() {
    return this.targetType;
  }

  @Override
  public DecContextFactory getDecContextFactory() {
    return decContextFactory;
  }

  public StringConverter getConverterOf(Class<?> from) {
    return stringConverterFactory.getCodecOf(from);
  }

}
