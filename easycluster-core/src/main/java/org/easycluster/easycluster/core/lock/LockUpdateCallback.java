package org.easycluster.easycluster.core.lock;

public interface LockUpdateCallback {

	void updateLockState(String lockId, LockStatus lockStatus, Object updateData);
}
