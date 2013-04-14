/**
 * 
 */
package org.easycluster.easycluster.cluster.common;

import org.easycluster.easycluster.core.Identifiable;
import org.easycluster.easycluster.core.Transformer;

public class KeyTransformer implements Transformer<Object, Object> {

	@Override
	public Object transform(Object from) {
		if (from instanceof Identifiable) {
			return ((Identifiable) from).getIdentification();
		}
		return null;
	}
}
