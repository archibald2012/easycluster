/**
 * 
 */
package org.easycluster.easycluster.serialization.kv.codec;

import java.util.List;
import java.util.Map;

import org.easycluster.easycluster.core.Transformer;


/**
 * @author Administrator
 * 
 */
public class KVEncoded2Url implements Transformer<Map<String, List<String>>, String> {

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

  @Override
  public String transform(Map<String, List<String>> from) {

    StringBuffer ret = new StringBuffer();

    for (Map.Entry<String, List<String>> e : from.entrySet()) {
      String key = e.getKey();
      List<String> list = e.getValue();

      if (!list.isEmpty()) {
        for (String v : list) {
          ret.append(separator).append(key).append(equal).append(v);
        }
      }

    }
    if (ret.length() > 0) {
      ret.deleteCharAt(0);// remove first separator
    }

    // hsman=skytest&hstype=m900&hswidth=240&hsheight=320&hsplat=mtk&version=153
    return ret.toString();
  }

}
