package configuration;

public class Table {
	private String name;
	private String folder;

	public Table() {
	}

	/**
	 * @return the folder
	 */
	public String getFolder() {
		return this.folder;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param folder
	 *            the folder to set
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	};
}