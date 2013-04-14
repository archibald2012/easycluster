/**
 * 
 */
package org.easycluster.easycluster.serialization.kv.codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easycluster.easycluster.core.Transformer;


/**
 * @author archie
 * 
 */
public class UrlDecoded2KV implements Transformer<String, Map<String, List<String>>> {

  private String separator = "&";
  private String equal     = "=";

  /**
   * @return the equal
   */
  public String getEqual() {
    return equal;
  }

  /**
   * @param equal
   *          the equal to set
   */
  public void setEqual(String equal) {
    this.equal = equal;
  }

  /**
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * @param separator
   *          the separator to set
   */
  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public Map<String, List<String>> transform(String from) {
    Map<String, List<String>> ret = new HashMap<String, List<String>>();

    // hsman=skytest&hstype=m900&hswidth=240&hsheight=320&hsplat=mtk&version=153
    int begin = 0;
    int end = 0;

    do {
      end = from.indexOf(separator, begin);
      if (-1 == end) {
        end = from.length();
      }
      String pair = from.substring(begin, end);
      begin = end + 1;

      // deal with pair
      int idx = pair.indexOf(equal);
      if (-1 != idx) {
        String key = pair.substring(0, idx).trim();
        String value = pair.substring(idx + 1);
        List<String> list = ret.get(key);
        if (null == list) {
          list = new ArrayList<String>();
          ret.put(key, list);
        }
        list.add(value);
      }
    } while (end < from.length());

    return ret;
  }

}
