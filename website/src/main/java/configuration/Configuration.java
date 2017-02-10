package configuration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.yaml.snakeyaml.Yaml;


public class Configuration {
	public static Configuration readYamlFile(String yamlFile)
			throws FileNotFoundException {
		return new Yaml().loadAs(new FileReader(yamlFile), Configuration.class);
	}

	private Database database;

	List<Corpus> corpora;

	public Configuration() {
	}

	/**
	 * @return the database
	 */
	public Database getDatabase() {
		return this.database;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}

	/**
	 * @return the corpora
	 */
	public List<Corpus> getCorpora() {
		return this.corpora;
	}

	/**
	 * @param corpora the corpora to set
	 */
	public void setCorpora(List<Corpus> corpora) {
		this.corpora = corpora;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Configuration [database=" + this.database + ", corpora="
				+ this.corpora + "]";
	}

}
