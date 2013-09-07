package org.easycluster.easycluster.serialization.bytebean.context;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;

/**
 * 
 * 
 * @author Archibald.Wang
 * @version $Id: FieldCodecContext.java 14 2012-01-10 11:54:14Z archie $
 */
public interface FieldCodecContext extends FieldCodecProvider {
	ByteFieldDesc getFieldDesc();

	Field getField();

	NumberCodec getNumberCodec();

	int getByteSize();
}
