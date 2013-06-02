package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.core.ByteUtil;
import org.junit.Test;

public class ByteBeanCodecTestCase {

	@Test
	public void test() {
		ByteBeanEncoder encoder = new ByteBeanEncoder();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127, (byte) 128 });
		
		byte[] bytes = encoder.transform(request);
		
		ByteUtil.bytesAsHexString(bytes, 1024);
	}

}
