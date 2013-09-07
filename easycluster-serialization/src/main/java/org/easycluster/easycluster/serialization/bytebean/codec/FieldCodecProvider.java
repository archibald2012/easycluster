package org.easycluster.easycluster.serialization.bytebean.codec;

/**
 * 
 * 
 * @author Archibald.Wang
 * @version $Id: FieldCodecProvider.java 14 2012-01-10 11:54:14Z archie $
 */
public interface FieldCodecProvider {
	ByteFieldCodec getCodecOf(FieldCodecCategory type);

	ByteFieldCodec getCodecOf(Class<?> clazz);
}
