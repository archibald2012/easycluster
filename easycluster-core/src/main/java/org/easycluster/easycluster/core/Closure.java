package org.easycluster.easycluster.core;

public interface Closure {

	/**
	 * Performs an action on the specified input object.
	 * 
	 * @param input
	 *            the input to execute on
	 */
	void execute(Object msg);
}
