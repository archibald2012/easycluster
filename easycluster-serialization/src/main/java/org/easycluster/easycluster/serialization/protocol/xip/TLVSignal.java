/**
 * 
 */
package org.easycluster.easycluster.serialization.protocol.xip;

import org.easycluster.easycluster.core.Identifiable;

public interface TLVSignal extends Identifiable {

	void setSourceId(short id);

	short getSourceId();
}
