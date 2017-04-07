package de.julielab.jeseme.configuration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

public class Configuration {
	public static Configuration readYamlFile(final String yamlFile)
			throws FileNotFoundException {
		return new Yaml().loadAs(new FileReader(yamlFile), Configuration.class);
	}

	private Database database;

	private Server server;

	private List<Corpus> corpora;

	public Configuration() {
	}

	public boolean coversServer() {
		return (server != null) && (server.getIp() != null)
				&& (server.getPort() != null);
	}

	/**
	 * @return the corpora
	 */
	public List<Corpus> getCorpora() {
		return corpora;
	}

	/**
	 * @return the database
	 */
	public Database getDatabase() {
		return database;
	}

	/**
	 * @return the server
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * @param corpora
	 *            the corpora to set
	 */
	public void setCorpora(final List<Corpus> corpora) {
		this.corpora = corpora;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(final Database database) {
		this.database = database;
	}

	/**
	 * @param server
	 *            the server to set
	 */
	public void setServer(final Server server) {
		this.server = server;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Configuration [database=" + database + ", serverConfig="
				+ server + ", corpora=" + corpora + "]";
	}

}
