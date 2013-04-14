/*******************************************************************************
 * CopyRight (c) 2005-2011 TAOTAOSOU Co, Ltd. All rights reserved.
 * Filename:    SampleAction.java
 * Creator:     wangqi
 * Create-Date: 2011-6-13 下午07:37:53
 *******************************************************************************/
package org.easycluster.easycluster.core.ebus;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: SampleEventUnit.java 14 2012-01-10 11:54:14Z archie $
 */
public class SampleEventUnit {

  private AtomicInteger count = new AtomicInteger(0);

  public void increase() {
    System.out.println("increase");
    count.incrementAndGet();
  }
  public void increase2() {
    System.out.println("increase2");
    count.incrementAndGet();
  }

  public void increaseByStep(int delta) {
    System.out.println("increaseByStep: " + delta);
    count.addAndGet(delta);
  }

  public void increaseBySignal(SampleSignal signal) {
    System.out.println("increaseBySignal: " + signal);
    count.addAndGet(signal.getIntField());
  }

  public AtomicInteger getCount() {
    return count;
  }

}
