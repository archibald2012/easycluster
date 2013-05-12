package org.easycluster.easycluster.core.lock;

public interface DistributedLock {

	void acquireLock(LockUpdateCallback updateCallback, Object callbackData);

	void releaseLock();
}
