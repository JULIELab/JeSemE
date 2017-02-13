package database.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Query;
import org.sql2o.Sql2o;

public abstract class Importer {
	static final long IMPORT_BATCH_SIZE = 100000;
	static final long IMPORT_COMMIT_SIZE = 10 * IMPORT_BATCH_SIZE;

	public static final String FREQUENCY_CSV = "FREQUENCY.csv";
	public static final String PPMI_CSV = "PPMI.csv";
	public static final String CHI_CSV = "CHI.csv";
	public static final String SIMILARITY_CSV = "SIMILARITY.csv";
	public static final String WORDS_CSV = "WORDIDS.csv";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Importer.class);

	private final String sql;

	private final String tableName;
	private final Sql2o sql2o;
	private final int corpusId;

	protected Importer(final String sqlTemplate, final Sql2o sql2o,
			final int corpusId, final String tableName) {
		this.sql = String.format(sqlTemplate, tableName);
		this.sql2o = sql2o;
		this.corpusId = corpusId;
		this.tableName = tableName;
	}

	abstract protected Query addParameters(final String[] s, final Query query);

	public void importStuff(final Path path) throws IOException {
		LOGGER.info("Starting import of {} into {}", path, tableName);
		final Iterator<String[]> iter = Files.lines(path).map(x -> x.split(","))
				.iterator();
		long i = 0;
		Query query = null;
		try {
			while (iter.hasNext()) {
				if (query == null)
					query = sql2o.beginTransaction()
							.createQuery(String.format(sql, tableName));

				addParameters(iter.next(), query)
						.addParameter("corpus", corpusId).addToBatch();

				//Nicer batch import leaks memory, thus using new connections is recommended
				++i;
				if ((i % IMPORT_BATCH_SIZE) == 0)
					query.executeBatch();
				if ((i % IMPORT_COMMIT_SIZE) == 0) {
					query.executeBatch().commit();
					LOGGER.info("Finishd import of {}", i);
					query = null;
				}
			}
		} finally {
			if (query != null)
				query.executeBatch().commit();
		}
		LOGGER.info("Finished import of {} rows from {} into {}", i, path, tableName);
	}
}
