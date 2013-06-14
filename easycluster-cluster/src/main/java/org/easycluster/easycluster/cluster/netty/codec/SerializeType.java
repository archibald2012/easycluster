package org.easycluster.easycluster.cluster.netty.codec;

public enum SerializeType {

	BINARY,
	
	TLV,
	
	JSON,
	
	/**
	 * Google Protocol Buffers
	 */
	PROTOBUF,
	
	JAVA,
	
}
