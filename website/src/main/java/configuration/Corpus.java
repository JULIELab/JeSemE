package configuration;

public class Corpus {
	private String name;
	private String path;
	private CorpusInfo info;

	public Corpus() {
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Corpus other = (Corpus) obj;
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	public CorpusInfo getInfo() {
		return info;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((info == null) ? 0 : info.hashCode());
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	public void setInfo(final CorpusInfo info) {
		this.info = info;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "Corpus [name=" + name + ", path=" + path + ", info=" + info + "]";
	}

}