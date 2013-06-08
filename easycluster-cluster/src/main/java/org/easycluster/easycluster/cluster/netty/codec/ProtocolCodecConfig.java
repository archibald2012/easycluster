package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.cluster.NetworkDefaults;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;

public class ProtocolCodecConfig {

	/**
	 * The serialize type, binary as default.
	 */
	private SerializeType			serializeType			= SerializeType.BINARY;

	/**
	 * The type meta info mapping.
	 */
	private MsgCode2TypeMetainfo	typeMetaInfo			= null;
	private boolean					encodeBytesDebugEnabled	= false;
	private boolean					decodeBytesDebugEnabled	= false;
	private int						dumpBytes				= 256;
	private String					encryptKey				= null;
	private int						maxContentLength		= NetworkDefaults.REQUEST_MAX_CONTENT_LENGTH;
	private int						lengthFieldOffset		= -1;
	private int						lengthFieldLength		= -1;

	public SerializeType getSerializeType() {
		return serializeType;
	}

	public void setSerializeType(SerializeType serializeType) {
		this.serializeType = serializeType;
	}

	public MsgCode2TypeMetainfo getTypeMetaInfo() {
		return typeMetaInfo;
	}

	public void setTypeMetaInfo(MsgCode2TypeMetainfo typeMetaInfo) {
		this.typeMetaInfo = typeMetaInfo;
	}

	public boolean isEncodeBytesDebugEnabled() {
		return encodeBytesDebugEnabled;
	}

	public void setEncodeBytesDebugEnabled(boolean encodeBytesDebugEnabled) {
		this.encodeBytesDebugEnabled = encodeBytesDebugEnabled;
	}

	public boolean isDecodeBytesDebugEnabled() {
		return decodeBytesDebugEnabled;
	}

	public void setDecodeBytesDebugEnabled(boolean decodeBytesDebugEnabled) {
		this.decodeBytesDebugEnabled = decodeBytesDebugEnabled;
	}

	public int getDumpBytes() {
		return dumpBytes;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}

	public String getEncryptKey() {
		return encryptKey;
	}

	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

	public int getLengthFieldOffset() {
		return lengthFieldOffset;
	}

	public void setLengthFieldOffset(int lengthFieldOffset) {
		this.lengthFieldOffset = lengthFieldOffset;
	}

	public int getLengthFieldLength() {
		return lengthFieldLength;
	}

	public void setLengthFieldLength(int lengthFieldLength) {
		this.lengthFieldLength = lengthFieldLength;
	}

}
