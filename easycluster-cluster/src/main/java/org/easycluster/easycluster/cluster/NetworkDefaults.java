package org.easycluster.easycluster.cluster;

/**
 * A container for defaults related to the networking code.
 */
public interface NetworkDefaults {

	/**
	 * The default maximum number of times to try to open a socket.
	 */
	int		MAX_RETRY_COUNT							= 20;

	/**
	 * The default number of milliseconds to wait for retry opening a socket.
	 */
	long	RETRY_TIMEOUT_MILLIS					= 30000;

	/**
	 * The default number of milliseconds to wait for retry opening a socket.
	 */
	int		ALLIDLE_TIMEOUT_MILLIS					= 90000;

	/**
	 * The default maximum number of connections to be opened per node.
	 */
	int		MAX_CONNECTIONS_PER_NODE				= 5;

	/**
	 * The default number of milliseconds to wait when opening a socket.
	 */
	int		CONNECT_TIMEOUT_MILLIS					= 1000;

	/**
	 * The default write timeout in milliseconds.
	 */
	int		WRITE_TIMEOUT_MILLIS					= 150;

	/**
	 * The default frequency to clean up stale requests in minutes.
	 */
	int		STALE_REQUEST_CLEANUP_FREQUENCY_MINS	= 10;

	/**
	 * The default length of time to wait before considering a request to be stale in minutes.
	 */
	int		STALE_REQUEST_TIMEOUT_MINS				= 10;

	/**
	 * The default number of core request threads.
	 */
	int		REQUEST_THREAD_CORE_POOL_SIZE			= Runtime.getRuntime().availableProcessors() * 2;

	/**
	 * The default max number of core request threads.
	 */
	int		REQUEST_THREAD_MAX_POOL_SIZE			= REQUEST_THREAD_CORE_POOL_SIZE * 5;

	/**
	 * The default request thread timeout in seconds.
	 */
	int		REQUEST_THREAD_KEEP_ALIVE_TIME_SECS		= 300;
}
