package org.easycluster.easycluster.serialization.kv.codec;

import junit.framework.Assert;

import org.easycluster.easycluster.serialization.kv.codec.DefaultKVCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KVCodecTestCase {

  private DefaultKVCodec kvCodec;

  @Before
  public void setUp() throws Exception {
    kvCodec = new DefaultKVCodec();
  }

  @After
  public void tearDown() throws Exception {
    kvCodec = null;
  }

  @Test
  public void testKVCodec_emptyValue() {
    String param = "";

    Object result = kvCodec.decode(kvCodec.getDecContextFactory().createDecContext(param, SampleKVBean.class, null));

    System.out.println(result);

    SampleKVBean bean = new SampleKVBean();

    Assert.assertEquals(bean, (SampleKVBean) result);

    result = kvCodec.encode(kvCodec.getEncContextFactory().createEncContext(bean, SampleKVBean.class));

    System.out.println(result);
  }

  @Test
  public void testKVCodec_decode() {
    String param = "hsman=skytest&hstype=m900&hswidth=240&hsheight=320&hsplat=mtk&version=153&version=154&byteField=127&shortField=128&booleanField=true";

    Object result = kvCodec.decode(kvCodec.getDecContextFactory().createDecContext(param, SampleKVBean.class, null));

    System.out.println(result);
    SampleKVBean bean = new SampleKVBean();
    bean.setHsman("skytest");
    bean.setHstype("m900");
    bean.setHsplat("mtk");
    bean.setHsheight(320);
    bean.setHswidth(240);
    bean.setVersion(new short[] { 153, 154 });
    bean.setByteField((byte) 127);
    bean.setShortField((short) 128);
    bean.setBooleanField(true);
    Assert.assertEquals(bean, (SampleKVBean) result);
  }

  @Test
  public void testKVCodec_encode() {

    SampleKVBean bean = new SampleKVBean();
    bean.setHsman("skytest");
    bean.setHstype("m900");
    bean.setHsplat("mtk");
    bean.setHsheight(320);
    bean.setHswidth(240);
    bean.setVersion(new short[] { 153, 154 });
    bean.setByteField((byte) 127);
    bean.setShortField((short) 128);
    bean.setBooleanField(true);

    String result = kvCodec.encode(kvCodec.getEncContextFactory().createEncContext(bean, SampleKVBean.class));

    System.out.println(result);
  }

}
