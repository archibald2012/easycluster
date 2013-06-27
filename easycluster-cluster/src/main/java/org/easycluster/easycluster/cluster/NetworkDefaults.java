package org.easycluster.easycluster.cluster;

public interface NetworkDefaults {

	int		MAX_RETRY_COUNT							= 20;

	long	RETRY_TIMEOUT_MILLIS					= 30000;

	int		ALLIDLE_TIMEOUT_MILLIS					= 90000;

	int		MAX_CONNECTIONS_PER_NODE				= 5;

	int		CONNECT_TIMEOUT_MILLIS					= 1000;

	int		WRITE_TIMEOUT_MILLIS					= 1000;

	int		STALE_REQUEST_CLEANUP_FREQUENCY_MINS	= 10;

	int		STALE_REQUEST_TIMEOUT_MINS				= 10;

	int		REQUEST_THREAD_CORE_POOL_SIZE			= Runtime.getRuntime().availableProcessors() * 2;

	int		REQUEST_THREAD_MAX_POOL_SIZE			= REQUEST_THREAD_CORE_POOL_SIZE * 5;

	int		REQUEST_THREAD_KEEP_ALIVE_TIME_SECS		= 5;

	int		REQUEST_MAX_CONTENT_LENGTH				= 10 * 1024 * 1024;
}
