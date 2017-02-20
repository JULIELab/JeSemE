package configuration;

public class Server {

	private Integer port;
	private String ip;

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param ip
	 *            the ip to set
	 */
	public void setIp(final String ip) {
		this.ip = ip;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(final Integer port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "sConfig [port=" + port + ", ip=" + ip + "]";
	}
}
