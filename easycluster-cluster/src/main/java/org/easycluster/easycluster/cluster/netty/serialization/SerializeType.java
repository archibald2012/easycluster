package org.easycluster.easycluster.cluster.netty.serialization;

/**
 * 一般情况下，序列化后的字节数 BYTE_BEAN < TLV < KV < JSON < JAVA < XML
 * 
 * @author wangqi
 * 
 */
public enum SerializeType {

	BYTE_BEAN,

	/**
	 * TLV(Tag,Length,Value)三元组，在数据通讯协议里，可选信息可以编码为type-length-value格式。T、
	 * L字段的长度往往固定（通常为1～4bytes），V字段长度可变。T用一个数字代码表示整个数据块的类型，L字段表示报文长度，V表示长度可变的数据区
	 */
	TLV,

	KV,

	JSON,

	JAVA,

}
