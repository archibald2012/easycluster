package org.easycluster.easycluster.cluster.netty.codec;

import java.util.ArrayList;
import java.util.List;

import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.Assert;
import org.junit.Test;

public class JsonBeanCodecTestCase {

	@Test
	public void test() {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		JsonBeanEncoder encoder = new JsonBeanEncoder();
		encoder.setDebugEnabled(true);

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127, (byte) 128 });

		byte[] bytes = encoder.transform(request);

		ByteUtil.bytesAsHexString(bytes, 1024);

		JsonBeanDecoder decoder = new JsonBeanDecoder();
		decoder.setDebugEnabled(true);
		decoder.setTypeMetaInfo(typeMetaInfo);

		SampleRequest assertobj = (SampleRequest) decoder.transform(bytes);

		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());
	}

}
