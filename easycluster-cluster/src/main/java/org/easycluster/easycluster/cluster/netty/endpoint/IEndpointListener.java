/**
 * 
 */
package org.easycluster.easycluster.cluster.netty.endpoint;

/**
 * @author wangqi
 * 
 */
public interface IEndpointListener {

  void onCreate(Endpoint endpoint);
  void onStop(Endpoint endpoint);
}
