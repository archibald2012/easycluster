package org.easycluster.easycluster.core.ebus;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.easycluster.easycluster.core.Functor;
import org.easycluster.easycluster.core.ebus.DefaultEventBus;
import org.easycluster.easycluster.core.ebus.Subscription;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class EventBusTestCase {
  private static final String INCREASE_BY_STEP = "increaseByStep";
  private static final String INCREASE_EVENT   = "increaseEvent";
  private DefaultEventBus     eventBus;

  @Before
  public void setUp() throws Exception {
    eventBus = new DefaultEventBus();
    eventBus.start();
  }

  @After
  public void tearDown() throws Exception {
    eventBus.destroy();
    eventBus = null;
  }

  @Test
  public void testSubscribe_duplicatedMethod() {
    SampleEventUnit target = new SampleEventUnit();

    Functor functor = new Functor(target, "increase");
    eventBus.subscribe(INCREASE_EVENT, functor);

    Map<String, Subscription> subscriptions = eventBus.getSubscriptions();

    Assert.assertEquals(1, subscriptions.size());
    Subscription functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor, functors.getClosures().get(0));

    Functor functor2 = new Functor(target, "increase");
    Assert.assertEquals(functor, functor2);

    eventBus.subscribe(INCREASE_EVENT, functor2);

    subscriptions = eventBus.getSubscriptions();

    Assert.assertEquals(1, subscriptions.size());

    functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor, functors.getClosures().get(0));

  }

  @Test
  public void testSubscribe_oneEventMoreSubscriber() {
    SampleEventUnit target = new SampleEventUnit();

    Functor functor = new Functor(target, "increase");
    eventBus.subscribe(INCREASE_EVENT, functor);

    Map<String, Subscription> subscriptions = eventBus.getSubscriptions();

    Assert.assertEquals(1, subscriptions.size());
    Subscription functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor, functors.getClosures().get(0));

    Functor functor2 = new Functor(target, "increase2");

    eventBus.subscribe(INCREASE_EVENT, functor2);

    subscriptions = eventBus.getSubscriptions();

    Assert.assertEquals(1, subscriptions.size());

    functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(2, functors.size());
    // function in sequence
    Assert.assertEquals(functor, functors.getClosures().get(0));
    Assert.assertEquals(functor2, functors.getClosures().get(1));
  }

  @Test
  public void testSubscribe_moreEventOneSubscriber() {
    SampleEventUnit target = new SampleEventUnit();

    Functor functor = new Functor(target, "increase");
    eventBus.subscribe(INCREASE_EVENT, functor);
    eventBus.subscribe("increaseEvent2", functor);

    Map<String, Subscription> subscriptions = eventBus.getSubscriptions();
    Assert.assertEquals(2, subscriptions.size());

    Subscription functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor, functors.getClosures().get(0));

    functors = subscriptions.get("increaseEvent2");
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor, functors.getClosures().get(0));
  }

  @Test
  public void testSubscribe_Normal() {
    SampleEventUnit target = new SampleEventUnit();

    Functor functor = new Functor(target, "increase");
    eventBus.subscribe(INCREASE_EVENT, functor);

    Map<String, Subscription> subscriptions = eventBus.getSubscriptions();

    Assert.assertEquals(1, subscriptions.size());
    Subscription functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor, functors.getClosures().get(0));

    Functor functor2 = new Functor(target, INCREASE_BY_STEP);
    eventBus.subscribe(INCREASE_BY_STEP, functor2);

    subscriptions = eventBus.getSubscriptions();

    Assert.assertEquals(2, subscriptions.size());

    functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor, functors.getClosures().get(0));

    functors = subscriptions.get(INCREASE_BY_STEP);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor2, functors.getClosures().get(0));
  }

  @Test
  public void testUnsubscribe() {
    SampleEventUnit target = new SampleEventUnit();

    Functor functor = new Functor(target, "increase");
    eventBus.subscribe(INCREASE_EVENT, functor);
    Functor functor2 = new Functor(target, INCREASE_BY_STEP);
    eventBus.subscribe(INCREASE_EVENT, functor2);

    eventBus.unsubscribe(INCREASE_EVENT, functor);

    Map<String, Subscription> subscriptions = eventBus.getSubscriptions();
    Assert.assertEquals(1, subscriptions.size());
    Subscription functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(1, functors.size());
    Assert.assertEquals(functor2, functors.getClosures().get(0));

    eventBus.unsubscribe(INCREASE_EVENT, functor2);

    subscriptions = eventBus.getSubscriptions();
    Assert.assertEquals(1, subscriptions.size());
    functors = subscriptions.get(INCREASE_EVENT);
    Assert.assertNotNull(functors);
    Assert.assertEquals(0, functors.size());
  }

  @Test
  public void testPublish() throws InterruptedException {

    SampleEventUnit target = new SampleEventUnit();

    eventBus.subscribe(INCREASE_EVENT, new Functor(target, "increase"));
    eventBus.subscribe(INCREASE_EVENT, new Functor(target, "increase2"));
    eventBus.subscribe(INCREASE_BY_STEP, new Functor(target, INCREASE_BY_STEP));

    eventBus.publish(INCREASE_EVENT);
    Thread.sleep(1000);
    Assert.assertEquals(2, target.getCount().intValue());

    eventBus.publish(INCREASE_BY_STEP, 20);
    Thread.sleep(1000);
    Assert.assertEquals(22, target.getCount().intValue());
  }

  @Test
  public void testPublish_noSubscriber() throws InterruptedException {
    // no subscriber
    eventBus.publish(INCREASE_EVENT);
    Thread.sleep(1000);
  }

  @Test
  public void testGetPendingTaskCount() throws InterruptedException {

    SampleEventUnit target = new SampleEventUnit();
    eventBus.subscribe(INCREASE_EVENT, new Functor(target, "increase"));

    final AtomicInteger count = new AtomicInteger(1000);
    Executors.newSingleThreadExecutor().execute(new Runnable() {

      @Override
      public void run() {
        while (count.intValue() > 0) {
          eventBus.publish(INCREASE_EVENT, new Object[] {});
          count.decrementAndGet();
        }
      }

    });

    while (count.intValue() > 0) {
      System.out.println(eventBus.getPendingTaskCount());
    }
  }
}
