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

	List<Table> tables;

	public Configuration() {
	}

	/**
	 * @return the database
	 */
	public Database getDatabase() {
		return this.database;
	}

	/**
	 * @return the tables
	 */
	public List<Table> getTables() {
		return this.tables;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}

	/**
	 * @param tables
	 *            the tables to set
	 */
	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

}
