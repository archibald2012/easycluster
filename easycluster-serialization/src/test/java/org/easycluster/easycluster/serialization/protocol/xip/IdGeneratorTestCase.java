package org.easycluster.easycluster.serialization.protocol.xip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

public class IdGeneratorTestCase {

	@Test
	public void testNextLong() throws InterruptedException, ExecutionException {

		ExecutorService exec = Executors.newFixedThreadPool(2);

		List<Future<List<Long>>> fList = new ArrayList<Future<List<Long>>>();
		for (int i = 0; i < 2; i++) {
			fList.add(exec.submit(new Callable<List<Long>>() {

				@Override
				public List<Long> call() throws Exception {
					final List<Long> result = new ArrayList<Long>();
					for (int i = 0; i < 1000; i++) {
						long nextLong = IdGenerator.nextLong();
						result.add(nextLong);
					}
					return result;
				}
			}));
		}

		final List<Long> result = new ArrayList<Long>();

		for (Future<List<Long>> f : fList) {
			result.addAll(f.get());
		}

		Collections.sort(result);

		for (int i = 0; i < 2000; i++) {
			Assert.assertEquals(i + 1, result.get(i).intValue());
		}
		Assert.assertEquals(2001, IdGenerator.nextLong());
	}

}
