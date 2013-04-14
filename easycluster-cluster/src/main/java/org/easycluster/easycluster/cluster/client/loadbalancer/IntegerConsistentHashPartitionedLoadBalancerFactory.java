package org.easycluster.easycluster.cluster.client.loadbalancer;

public class IntegerConsistentHashPartitionedLoadBalancerFactory extends
		ConsistentHashPartitionedLoadBalancerFactory<Integer> {

	public IntegerConsistentHashPartitionedLoadBalancerFactory(int numPartitions) {
		super(numPartitions);
	}

	@Override
	protected int hashPartitionedId(Integer i) {
		return i.hashCode();
	}
}
