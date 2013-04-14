/**
 * 
 */
package org.easycluster.easycluster.serialization.kv.codec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easycluster.easycluster.serialization.kv.codec.UrlDecoded2KV;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author archie
 * 
 */
public class Urlencoded2KVTestCase {

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
    UrlDecoded2KV trans = new UrlDecoded2KV();
    Map<String, List<String>> ret = trans.transform("hsman=skytest&hstype=m900&hswidth=240&hsheight=320&hsplat=mtk&version=153");

    Map<String, List<String>> match = new HashMap<String, List<String>>();
    match.put("hsman", Arrays.asList(new String[] { "skytest" }));
    match.put("hstype", Arrays.asList(new String[] { "m900" }));
    match.put("hswidth", Arrays.asList(new String[] { "240" }));
    match.put("hsheight", Arrays.asList(new String[] { "320" }));
    match.put("hsplat", Arrays.asList(new String[] { "mtk" }));
    match.put("version", Arrays.asList(new String[] { "153" }));

    Assert.assertEquals(ret, match);
  }

}
