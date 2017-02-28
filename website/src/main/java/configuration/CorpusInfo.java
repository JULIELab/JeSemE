package configuration;

public class CorpusInfo {
	private String mappingPath;
	private Boolean lowercase;
	private String fullName;
	private String note;
	private Boolean insertInUrl;
	private String url;

	public CorpusInfo() {

	}

	public String getFullName() {
		return fullName;
	}

	public Boolean getInsertInUrl() {
		return (insertInUrl != null) && insertInUrl;
	}

	public Boolean getLowercase() {
		return (lowercase != null) && lowercase;
	}

	public String getMappingPath() {
		return mappingPath;
	}

	public String getNote() {
		return note;
	}

	public String getUrl() {
		return url;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	public void setInsertInUrl(final Boolean insertInUrl) {
		this.insertInUrl = insertInUrl;
	}

	public void setLowercase(final Boolean lowercase) {
		this.lowercase = lowercase;
	}

	public void setMappingPath(final String mappingPath) {
		this.mappingPath = mappingPath;
	}

	public void setNote(final String note) {
		this.note = note;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "CorpusInfo [mappingPath=" + mappingPath + ", lowercase=" + lowercase + ", fullName=" + fullName
				+ ", note=" + note + ", insertInUrl=" + insertInUrl + ", url=" + url + "]";
	}

}
