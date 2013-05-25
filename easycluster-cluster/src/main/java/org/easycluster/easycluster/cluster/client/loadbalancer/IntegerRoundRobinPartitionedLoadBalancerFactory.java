package org.easycluster.easycluster.cluster.client.loadbalancer;

public class IntegerRoundRobinPartitionedLoadBalancerFactory extends RoundRobinPartitionedLoadBalancerFactory<Integer> {

	@Override
	protected int hashPartitionedId(Integer i) {
		return i.hashCode();
	}
}
