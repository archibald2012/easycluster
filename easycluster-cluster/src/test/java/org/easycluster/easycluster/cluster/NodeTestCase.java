package org.easycluster.easycluster.cluster;

import org.easycluster.easycluster.cluster.common.XmlUtil;
import org.junit.Test;

public class NodeTestCase {

	@Test
	public void test() {
		Node node = new Node("localhost", 1111, new int[] { 0, 1 }, true);

		String output = XmlUtil.marshal(node);
		System.out.println(output);

		Node assertobj = XmlUtil.unmarshal(output, Node.class);
		System.out.println(assertobj);
	}

}
