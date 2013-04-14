package org.easycluster.easycluster.serialization.kv.context;

import org.easycluster.easycluster.serialization.kv.codec.StringConverterFactory;
import org.easycluster.easycluster.serialization.kv.codec.StringConverters;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: DefaultDecContextFactory.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultDecContextFactory implements DecContextFactory {

  private StringConverterFactory stringConverterFactory = StringConverters.getCommonFactory();

  public DecContext createDecContext(String decString, Class<?> targetType, Object parent) {
    return new DefaultDecContext().setStringConverterFactory(stringConverterFactory).setDecString(decString).setDecClass(targetType).setDecOwner(parent)
        .setDecContextFactory(this);
  }

  public StringConverterFactory getStringConverterFactory() {
    return stringConverterFactory;
  }

  public void setStringConverterFactory(StringConverterFactory stringConverterFactory) {
    this.stringConverterFactory = stringConverterFactory;
  }

}
