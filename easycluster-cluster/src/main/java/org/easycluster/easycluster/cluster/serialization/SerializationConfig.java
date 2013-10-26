package org.easycluster.easycluster.cluster.serialization;

import org.easycluster.easycluster.cluster.NetworkDefaults;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;

public class SerializationConfig {

	/**
	 * The serialize type, binary as default.
	 */
	private SerializeType		serializeType			= SerializeType.BYTE_BEAN;

	/**
	 * The type meta info mapping.
	 */
	private Int2TypeMetainfo	typeMetaInfo			= null;

	private boolean				encodeBytesDebugEnabled	= false;

	private boolean				decodeBytesDebugEnabled	= false;

	private int					dumpBytes				= 256;

	private String				encryptKey				= null;

	private int					maxContentLength		= NetworkDefaults.REQUEST_MAX_CONTENT_LENGTH;

	public SerializeType getSerializeType() {
		return serializeType;
	}

	public void setSerializeType(SerializeType serializeType) {
		this.serializeType = serializeType;
	}

	public Int2TypeMetainfo getTypeMetaInfo() {
		return typeMetaInfo;
	}

	public void setTypeMetaInfo(Int2TypeMetainfo typeMetaInfo) {
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

}
