package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;

public interface SerializationFactory {

	Transformer<XipSignal, byte[]> getEncoder();

	Transformer<byte[], XipSignal> getDecoder();
}
