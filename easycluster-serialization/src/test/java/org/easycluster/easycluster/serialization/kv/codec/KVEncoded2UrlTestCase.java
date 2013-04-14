/**
 * 
 */
package org.easycluster.easycluster.serialization.kv.codec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easycluster.easycluster.serialization.kv.codec.KVEncoded2Url;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author archie
 * 
 */
public class KVEncoded2UrlTestCase {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testTransform() {
    KVEncoded2Url trans = new KVEncoded2Url();

    Map<String, List<String>> from = new HashMap<String, List<String>>();
    from.put("hsman", Arrays.asList(new String[] { "skytest" }));
    from.put("hstype", Arrays.asList(new String[] { "m900" }));
    from.put("hswidth", Arrays.asList(new String[] { "240" }));
    from.put("hsheight", Arrays.asList(new String[] { "320" }));
    from.put("hsplat", Arrays.asList(new String[] { "mtk" }));
    from.put("version", Arrays.asList(new String[] { "153", "154" }));

    String assertobj = trans.transform(from);
    System.out.println(assertobj);
  }

}
