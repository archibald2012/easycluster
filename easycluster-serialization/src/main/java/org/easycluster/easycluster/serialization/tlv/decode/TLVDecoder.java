/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode;

public interface TLVDecoder {
	Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx);
}
