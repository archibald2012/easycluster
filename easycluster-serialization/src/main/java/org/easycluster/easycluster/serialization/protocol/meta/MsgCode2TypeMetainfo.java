
package org.easycluster.easycluster.serialization.protocol.meta;


/**
 * TODO
 * @author wangqi
 * @version $Id: MsgCode2TypeMetainfo.java 14 2012-01-10 11:54:14Z archie $
 */
public interface MsgCode2TypeMetainfo {
	Class<?> find(int value);
}
