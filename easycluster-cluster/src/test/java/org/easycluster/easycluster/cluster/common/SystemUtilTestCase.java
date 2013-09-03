package org.easycluster.easycluster.cluster.common;

import org.junit.Test;

public class SystemUtilTestCase {

	@Test
	public void testGetIp() {
		System.out.println(SystemUtil.getIpAddress());
	}
	
	@Test
	public void testGetHostName() {
		System.out.println(SystemUtil.getHostName());
	}
	
	@Test
	public void testGetPid() {
		System.out.println(SystemUtil.getPid());
	}

	@Test
	public void testGetUserName() {
		System.out.println(SystemUtil.getUserName());
	}


}
