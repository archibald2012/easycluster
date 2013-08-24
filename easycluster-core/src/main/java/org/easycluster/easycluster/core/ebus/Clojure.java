package org.easycluster.easycluster.core.ebus;

public interface Clojure {

	/**
	 * Performs an action on the specified input object.
	 * 
	 * @param args
	 *            the input to execute on
	 */
	Object execute(Object... args);
}
