package de.julielab.jeseme.database.importer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Query;
import org.sql2o.Sql2o;

public abstract class Importer {
	static class GZIPFiles {
		private static void closeSafely(final Closeable closeable) {
			if (closeable != null)
				try {
					closeable.close();
				} catch (final IOException e) {
					// Ignore
				}
		}

		/**
		 * Get a lazily loaded stream of lines from a gzipped file, similar to
		 * {@link Files#lines(java.nio.file.Path)}.
		 *
		 * From
		 * https://erikwramner.wordpress.com/2014/05/02/lazily-read-lines-from-gzip-file-with-java-8-streams/
		 *
		 * @param path
		 *            The path to the gzipped file.
		 * @return stream with lines.
		 */
		public static Stream<String> lines(final Path path) {
			InputStream fileIs = null;
			BufferedInputStream bufferedIs = null;
			GZIPInputStream gzipIs = null;
			try {
				fileIs = Files.newInputStream(path);
				// Even though GZIPInputStream has a buffer it reads individual bytes
				// when processing the header, better add a buffer in-between
				bufferedIs = new BufferedInputStream(fileIs, 65535);
				gzipIs = new GZIPInputStream(bufferedIs);
			} catch (final IOException e) {
				closeSafely(gzipIs);
				closeSafely(bufferedIs);
				closeSafely(fileIs);
				throw new UncheckedIOException(e);
			}
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(gzipIs));
			return reader.lines().onClose(() -> closeSafely(reader));
		}
	}

	static final long IMPORT_BATCH_SIZE = 100000;

	static final long IMPORT_COMMIT_SIZE = 10 * IMPORT_BATCH_SIZE;
	public static final String FREQUENCY_CSV = "FREQUENCY.csv";
	public static final String EMBEDDING_CSV = "EMBEDDING.csv";
	public static final String PPMI_CSV = "PPMI.csv";
	public static final String CHI_CSV = "CHI.csv";
	public static final String SIMILARITY_CSV = "SIMILARITY.csv";
	public static final String EMOTION_CSV = "EMOTION.csv";
	public static final String WORDS_CSV = "WORDIDS.csv";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Importer.class);

	private final String sql;
	private final String tableName;
	private final Sql2o sql2o;

	private final int corpusId;

	protected Importer(final String sqlTemplate, final Sql2o sql2o,
			final int corpusId, final String tableName) {
		sql = String.format(sqlTemplate, tableName);
		this.sql2o = sql2o;
		this.corpusId = corpusId;
		this.tableName = tableName;
	}

	abstract protected Query addParameters(final String[] s, final Query query);

	public void importStuff(Path path) throws IOException {
		Stream<String> stream;
		if (!path.toFile().exists()
				&& new File(path.toFile().getPath() + ".gz").exists()) {
			path = Paths.get(path.toFile().getPath() + ".gz");
			stream = GZIPFiles.lines(path);
		} else
			stream = Files.lines(path);
		LOGGER.info("Starting import of {} into {}", path, tableName);
		final Iterator<String[]> iter = stream.map(x -> x.split(","))
				.iterator();
		long i = 1; //error if 0 due to criteria below
		Query query = null;
		try {
			while (iter.hasNext()) {
				if (query == null)
					query = sql2o.beginTransaction()
							.createQuery(String.format(sql, tableName));

				final String[] data = iter.next();
				addParameters(data, query).addParameter("corpus", corpusId)
						.addToBatch();
				//Nicer batch import leaks memory, thus using new connections is recommended
				++i;

				if ((i % IMPORT_BATCH_SIZE) == 0)
					query.executeBatch();
				if ((i % IMPORT_COMMIT_SIZE) == 0) {
					query.getConnection().commit();
					LOGGER.info("Finishd import of {}", i);
					query = null;
				}
			}
		} finally {
			if (query != null)
				query.executeBatch().commit();
		}
		LOGGER.info("Finished import of {} rows from {} into {}", i, path,
				tableName);
	}
}
