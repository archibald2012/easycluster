package org.easycluster.easycluster.cluster.netty.serialization;

public enum SerializeType {

	BINARY,

	/**
	 * TLV(Tag,Length,Value)三元组，在数据通讯协议里，可选信息可以编码为type-length-value格式。T、
	 * L字段的长度往往固定（通常为1～4bytes），V字段长度可变。T用一个数字代码表示整个数据块的类型，L字段表示报文长度，V表示长度可变的数据区
	 */
	TLV,

	KV,

	JSON,

	XML,

	/**
	 * Google Protocol Buffers
	 */
	PROTOBUF,

	JAVA,

}
