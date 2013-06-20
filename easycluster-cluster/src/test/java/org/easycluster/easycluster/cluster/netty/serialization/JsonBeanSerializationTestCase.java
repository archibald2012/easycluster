package org.easycluster.easycluster.cluster.netty.serialization;

import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.netty.serialization.JsonBeanSerialization;
import org.easycluster.easycluster.core.ByteUtil;
import org.junit.Assert;
import org.junit.Test;

public class JsonBeanSerializationTestCase {

	@Test
	public void test() {

		JsonBeanSerialization encoder = new JsonBeanSerialization();
		encoder.setDebugEnabled(true);

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127, (byte) 128 });

		byte[] bytes = encoder.serialize(request);

		ByteUtil.bytesAsHexString(bytes, 1024);

		SampleRequest assertobj = (SampleRequest) encoder.deserialize(bytes, SampleRequest.class);

		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());
	}

}
