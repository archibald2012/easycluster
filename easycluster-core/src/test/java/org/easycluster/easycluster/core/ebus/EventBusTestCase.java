package org.easycluster.easycluster.core.ebus;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventBusTestCase {
	private static final String	INCREASE_BY_STEP	= "increaseByStep";
	private static final String	INCREASE_EVENT		= "increaseEvent";
	private DefaultEventBus		eventBus;

	@Before
	public void setUp() throws Exception {
		eventBus = new DefaultEventBus();
	}

	@After
	public void tearDown() throws Exception {
		eventBus = null;
	}

	@Test
	public void testSubscribe_duplicatedMethod() {
		SampleEventUnit target = new SampleEventUnit();

		eventBus.subscribe(INCREASE_EVENT, target, "increase");

		Map<String, Subscription> subscriptions = eventBus.getSubscriptions();

		Assert.assertEquals(1, subscriptions.size());
		Subscription functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());

		eventBus.subscribe(INCREASE_EVENT, target, "increase");

		subscriptions = eventBus.getSubscriptions();

		Assert.assertEquals(1, subscriptions.size());

		functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());

	}

	@Test
	public void testSubscribe_oneEventMoreSubscriber() {
		SampleEventUnit target = new SampleEventUnit();

		eventBus.subscribe(INCREASE_EVENT, target, "increase");

		Map<String, Subscription> subscriptions = eventBus.getSubscriptions();

		Assert.assertEquals(1, subscriptions.size());
		Subscription functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());

		eventBus.subscribe(INCREASE_EVENT, target, "increase2");

		subscriptions = eventBus.getSubscriptions();

		Assert.assertEquals(1, subscriptions.size());

		functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(2, functors.size());
		// function in sequence
		Assert.assertEquals(new Functor(target, "increase"), functors.getClojures().get(0));
		Assert.assertEquals(new Functor(target, "increase2"), functors.getClojures().get(1));
	}

	@Test
	public void testSubscribe_moreEventOneSubscriber() {
		SampleEventUnit target = new SampleEventUnit();

		eventBus.subscribe(INCREASE_EVENT, target, "increase");
		eventBus.subscribe("increaseEvent2", target, "increase");

		Map<String, Subscription> subscriptions = eventBus.getSubscriptions();
		Assert.assertEquals(2, subscriptions.size());

		Subscription functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());
		Assert.assertEquals(new Functor(target, "increase"), functors.getClojures().get(0));

		functors = subscriptions.get("increaseEvent2");
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());
		Assert.assertEquals(new Functor(target, "increase"), functors.getClojures().get(0));
	}

	@Test
	public void testSubscribe_Normal() {
		SampleEventUnit target = new SampleEventUnit();

		eventBus.subscribe(INCREASE_EVENT, target, "increase");

		Map<String, Subscription> subscriptions = eventBus.getSubscriptions();

		Assert.assertEquals(1, subscriptions.size());
		Subscription functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());

		eventBus.subscribe(INCREASE_BY_STEP, target, INCREASE_BY_STEP);

		subscriptions = eventBus.getSubscriptions();

		Assert.assertEquals(2, subscriptions.size());

		functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());
		Assert.assertEquals(new Functor(target, "increase"), functors.getClojures().get(0));

		functors = subscriptions.get(INCREASE_BY_STEP);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());
		Assert.assertEquals(new Functor(target, INCREASE_BY_STEP), functors.getClojures().get(0));
	}

	@Test
	public void testUnsubscribe() {
		SampleEventUnit target = new SampleEventUnit();

		eventBus.subscribe(INCREASE_EVENT, target, "increase");
		eventBus.subscribe(INCREASE_EVENT, target, INCREASE_BY_STEP);

		eventBus.unsubscribe(INCREASE_EVENT, target, "increase");

		Map<String, Subscription> subscriptions = eventBus.getSubscriptions();
		Assert.assertEquals(1, subscriptions.size());
		Subscription functors = subscriptions.get(INCREASE_EVENT);
		Assert.assertNotNull(functors);
		Assert.assertEquals(1, functors.size());

		eventBus.unsubscribe(INCREASE_EVENT, target, INCREASE_BY_STEP);

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
		Assert.assertEquals(2, target.getCount().intValue());

		eventBus.publish(INCREASE_BY_STEP, 20);
		Assert.assertEquals(22, target.getCount().intValue());
	}

	@Test
	public void testPublish_noSubscriber() throws InterruptedException {
		// no subscriber
		eventBus.publish(INCREASE_EVENT, new Object[0]);
		Thread.sleep(1000);
	}

}
