/**
 * 
 */
package org.easycluster.easycluster.core;


public class KeyTransformer implements Transformer<Object, Object> {

	@Override
	public Object transform(Object from) {
		if (from instanceof Identifiable) {
			return ((Identifiable) from).getIdentification();
		}
		return null;
	}
}
