
package org.easycluster.easycluster.core;

import java.util.Map;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: Propertyable.java 14 2012-01-10 11:54:14Z archie $
 */
public interface Propertyable {
	Object getProperty(String key);
	Map<String, Object> getProperties();
	void setProperty(String key, Object value);
	void setProperties(Map<String, Object> properties);
}
