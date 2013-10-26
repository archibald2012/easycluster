package org.easycluster.easycluster.http;

import org.easycluster.easycluster.core.KeyTransformer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class HttpKeyTransformer extends KeyTransformer {

	@Override
	public Object transform(Object from) {
		if (from instanceof HttpRequest) {
			return ((HttpRequest) from).getHeader("uuid");
		} else if (from instanceof HttpResponse) {
			return ((HttpResponse) from).getHeader("uuid");
		}
		return null;
	}

}
