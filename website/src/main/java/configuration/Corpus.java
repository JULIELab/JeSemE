package configuration;

public class Corpus {
	private String name;
	private String path;
	private String mappingPath;

	public Corpus() {
	}

	public String getMappingPath() {
		return mappingPath;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	public void setMappingPath(final String mappingPath) {
		this.mappingPath = mappingPath;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(final String path) {
		this.path = path;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Corpus [name=" + name + ", path=" + path + ", mappingPath="
				+ mappingPath + "]";
	}

}