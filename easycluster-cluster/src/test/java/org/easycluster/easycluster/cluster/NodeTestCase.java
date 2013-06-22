package org.easycluster.easycluster.cluster;

import java.util.Arrays;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class NodeTestCase {

	@Test
	public void test() {
		Node node = new Node("localhost", 1111);
		node.setPartitions(Arrays.asList(new Integer[] { 0, 1 }));
		node.setServiceGroup("app");
		node.setService("test");
		String output = JSON.toJSONString(node);
		System.out.println(output);

		Node assertobj = JSON.parseObject(output, Node.class);
		System.out.println(assertobj);
	}
}
