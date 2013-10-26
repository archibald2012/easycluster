package org.easycluster.easycluster.cluster.netty.serialization;

import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.serialization.BeanKvSerialization;
import org.easycluster.easycluster.core.ByteUtil;
import org.junit.Assert;
import org.junit.Test;

public class BeanKvSerializationTestCase {

	@Test
	public void test() {

		BeanKvSerialization encoder = new BeanKvSerialization();
		encoder.setDebugEnabled(true);
		encoder.setEncryptKey("izmFNROXQ98C3w3T8tTiDD/ril0TlAzJGuEY+WiagsN19YPz3ewZJPIsLH4JBp2mUDwr5mfv1y7mPNLAnnndSQbklhcpK/aZXJfO7xxuLt2Z9/xRX7J6DcxlHa9LTOfhloXHrlFeOVGbX1O8Et4t6DvFdZOh2SIendLNsF");

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("中文");

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
