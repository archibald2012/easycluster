/**
 * 
 */
package org.easycluster.easycluster.serialization.kv.codec;

import org.easycluster.easycluster.serialization.kv.context.DecContext;
import org.easycluster.easycluster.serialization.kv.context.DecContextFactory;
import org.easycluster.easycluster.serialization.kv.context.EncContext;
import org.easycluster.easycluster.serialization.kv.context.EncContextFactory;

/**
 * @author archie
 * 
 */
public interface KVCodec {

  DecContextFactory getDecContextFactory();

  Object decode(DecContext ctx);
  
  EncContextFactory getEncContextFactory();
  
  String encode(EncContext ctx);
}
