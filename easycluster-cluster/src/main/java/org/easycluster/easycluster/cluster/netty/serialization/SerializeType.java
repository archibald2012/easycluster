package org.easycluster.easycluster.cluster.netty.serialization;

/**
 * bytes length: BYTE_BEAN < TLV < KV < JSON < JAVA < XML
 * 
 * @author wangqi
 * 
 */
public enum SerializeType {

	/**
	 * 
	 */
	BYTE_BEAN,

	/**
	 * TLV(Tag,Length,Value)
	 */
	TLV,

	/**
	 * 
	 */
	KV,

	/**
	 * 
	 */
	JSON,

	/**
	 * 
	 */
	JAVA,

}
