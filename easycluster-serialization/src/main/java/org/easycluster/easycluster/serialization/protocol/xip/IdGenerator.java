package org.easycluster.easycluster.serialization.protocol.xip;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Archibald.Wang
 */
public class IdGenerator {

	private static AtomicLong	transactionSeq	= new AtomicLong(1);

	private IdGenerator() {
	}

	public static long nextLong() {
		return transactionSeq.getAndIncrement();
	}
}
