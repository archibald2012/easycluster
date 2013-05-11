/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode;

import java.util.List;

public interface TLVEncoder {
	List<byte[]> encode(Object src, TLVEncodeContext ctx);
}
