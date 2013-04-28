package org.easycluster.easycluster.cluster.common;

import org.junit.Test;

public class SystemUtilTestCase {

	@Test
	public void testGetIp() {
		System.out.println(SystemUtil.getIpAddress());
	}
}
