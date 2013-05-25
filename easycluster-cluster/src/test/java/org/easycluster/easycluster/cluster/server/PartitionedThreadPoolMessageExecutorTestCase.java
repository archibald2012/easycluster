package org.easycluster.easycluster.cluster.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.core.Closure;
import org.junit.Test;

public class PartitionedThreadPoolMessageExecutorTestCase {

	@Test
	public void test() throws Exception {

		MessageClosureRegistry messageClosureRegistry = new MessageClosureRegistry();
		messageClosureRegistry.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());

		MessageExecutor messageExecutor = new PartitionedThreadPoolMessageExecutor(messageClosureRegistry, 1, 1, 10, 5);

		int num = 10000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();
		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			final int client = 1;
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setClient(client);
			request.setByteArrayField(new byte[] { 127 });
			client1Requests.add(request);
		}

		List<SampleRequest> client2Requests = new ArrayList<SampleRequest>();
		final List<SampleResponse> client2Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			final int client = 2;
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setClient(client);
			request.setByteArrayField(new byte[] { 127 });
			client2Requests.add(request);
		}

		long startTime = System.nanoTime();
		final CountDownLatch latch = new CountDownLatch(num * 2);

		for (int i = 0; i < num; i++) {

			final SampleRequest req = client1Requests.get(i);
			messageExecutor.execute(req, new Closure() {

				@Override
				public void execute(Object msg) {
					SampleResponse resp = (SampleResponse) msg;
					System.out.println("client" + req.getClient() + " receive resp, sequence: " + resp.getIdentification());
					client1Responses.add(resp);
					latch.countDown();
				}
			});

			final SampleRequest req2 = client2Requests.get(i);
			messageExecutor.execute(req, new Closure() {

				@Override
				public void execute(Object msg) {
					SampleResponse resp = (SampleResponse) msg;
					System.out.println("client" + req2.getClient() + " receive resp, sequence: " + resp.getIdentification());
					client2Responses.add(resp);
					latch.countDown();
				}
			});

		}

		latch.await();

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		Assert.assertEquals(num, client1Responses.size());
		Assert.assertEquals(num, client2Responses.size());
		for (int i = 0; i < num - 1; i++) {
			Assert.assertTrue(client1Responses.get(i).getIdentification() < client1Responses.get(i + 1).getIdentification());
		}
		for (int i = 0; i < num - 1; i++) {
			Assert.assertTrue(client2Responses.get(i).getIdentification() < client2Responses.get(i + 1).getIdentification());
		}
	}

}
