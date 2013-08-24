package org.easycluster.easycluster.serialization.protocol.xip;


public class AbstractXipSignal extends DefaultPropertiesSupport implements XipSignal {

	private long	sequence	= IdGenerator.nextLong();
	private long	client		= 0;

	@Override
	public void setIdentification(long sequence) {
		this.sequence = sequence;
	}

	@Override
	public long getIdentification() {
		return this.sequence;
	}

	public void setClient(long client) {
		this.client = client;
	}

	public long getClient() {
		return client;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) sequence;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractXipSignal other = (AbstractXipSignal) obj;
		if (sequence != other.sequence)
			return false;
		return true;
	}

}
