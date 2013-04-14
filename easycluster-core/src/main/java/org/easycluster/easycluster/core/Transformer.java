package org.easycluster.easycluster.core;

public interface Transformer<FROM, TO> {
	TO transform(FROM from);
}
