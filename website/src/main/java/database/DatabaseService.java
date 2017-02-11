package database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import configuration.Configuration;

/**
 *
 * @author hellrich
 *
 */

public class DatabaseService {
	private static final String[] IMPORT_MAPPING_ASSOCIATION = new String[] {
			"word1", "word2", "year", "association" };
	private static final String[] IMPORT_MAPPING_FREQUENCY = new String[] {
			"word", "year", "frequency" };
	private static final String[] IMPORT_MAPPING_WORDS = new String[] { "word",
			"id" };

	private static final Class<?>[] IMPORT_CLASSES_ASSOCIATION = new Class[] {
			Integer.class, Integer.class, Integer.class, Float.class };
	private static final Class<?>[] IMPORT_CLASSES_FREQUENCY = new Class[] {
			Integer.class, Integer.class, Float.class };
	private static final Class<?>[] IMPORT_CLASSES_WORDS = new Class[] {
			String.class, Integer.class };

	private static final String IMPORT_SQL_WORDS = "INSERT INTO %s (corpus, word, id) VALUES (:corpus, :word, :id)";

	private static final String IMPORT_SQL_FREQUENCY = "INSERT INTO %s (corpus, word, year, frequency) VALUES (:corpus, :word, :year, :frequency)";

	private static final String IMPORT_SQL_ASSOCIATION = "INSERT INTO %s (corpus, word1, word2, year, association) VALUES (:corpus, :word1, :word2, :year, :association)";

	private static final String FREQUENCY_CSV = "FREQUENCY.csv";
	private static final String PPMI_CSV = "PPMI.csv";
	private static final String CHI_CSV = "CHI.csv";
	private static final String SIMILARITY_CSV = "SIMILARITY.csv";
	private static final String WORDS_CSV = "WORDIDS.csv";

	private static final String SCHEMA = "JEDISEM";
	private static final String CORPORA = SCHEMA + ".TABLES";
	public static final String SIMILARITY_TABLE = SCHEMA + ".SIMILARITY";
	private static final String WORDIDS_TABLE = SCHEMA + ".WORDIDS";
	public static final String PPMI_TABLE = SCHEMA + ".PPMI";
	public static final String CHI_TABLE = SCHEMA + ".CHI";
	private static final String FREQUENCY_TABLE = SCHEMA + ".FREQUENCY";

	private static final int IMPORT_BATCH_SIZE = 10000;

	private static final String SIMILARITY_QUERY = "SELECT year, association AS value FROM "
			+ "%s"
			+ " WHERE corpus=:corpus AND (word1=:word1 AND word2=:word2) ORDER BY year ASC";
	private static final String YEARS_QUERY = "SELECT DISTINCT year FROM "
			+ SIMILARITY_TABLE
			+ " WHERE corpus=:corpus AND (word1=:word OR word2=:word) ORDER BY year";
	private static final String MOST_SIMILAR_QUERY = "SELECT word1, word2 FROM "
			+ SIMILARITY_TABLE
			+ " WHERE corpus=:corpus AND (word1=:givenWord OR word2=:givenWord) AND year=:year ORDER BY association DESC LIMIT :limit";
	private static final String TOP_CONTEXT_QUERY = "SELECT word2 FROM %s WHERE corpus=:corpus AND word1=:givenWord AND year=:year ORDER BY association DESC LIMIT :limit";
	private static final String FREQUENCY_QUERY = "SELECT year, frequency AS value FROM "
			+ FREQUENCY_TABLE
			+ " WHERE corpus=:corpus AND word=:word ORDER BY year ASC";

	private static void importStuff(final Sql2o sql2o, final Path path,
			final String tableName, final Integer corpus, final String sql,
			final String[] parameterMapping, final Class<?>[] classes)
			throws IOException {
		try (Connection con = sql2o.beginTransaction()) {
			final org.sql2o.Query query = con
					.createQuery(String.format(sql, tableName));
			int i = 0;
			//see http://www.lambdafaq.org/how-do-i-turn-a-stream-into-an-iterable/
			for (final String[] s : (Iterable<String[]>) Files.lines(path)
					.map(x -> x.split(","))::iterator) {
				for (int j = 0; j < parameterMapping.length; ++j) {
					if (classes[j] == String.class)
						query.addParameter(parameterMapping[j], s[j]);
					else if (classes[j] == Integer.class)
						query.addParameter(parameterMapping[j],
								Integer.valueOf(s[j]));
					else
						query.addParameter(parameterMapping[j],
								Float.valueOf(s[j]));
				}

				query.addParameter("corpus", corpus).addToBatch();
				if (i > IMPORT_BATCH_SIZE) {
					i = 0;
					query.executeBatch();
				}
			}
			query.executeBatch();
			con.commit(); // avoids rollback.
		}
	}

	public static void importTables(final Configuration config,
			final Sql2o sql2o) throws Exception {

		for (final configuration.Corpus corpus : config.getCorpora()) {
			final String corpusName = corpus.getName();
			final Path path = Paths.get(corpus.getPath());

			try (Connection con = sql2o.open()) {
				final int corpusId = (int) con
						.createQuery("INSERT INTO " + CORPORA
								+ " (corpus) VALUES (:corpus);", true)
						.addParameter("corpus", corpusName).executeUpdate()
						.getKey();
				importStuff(sql2o, path.resolve(WORDS_CSV), WORDIDS_TABLE,
						corpusId, IMPORT_SQL_WORDS, IMPORT_MAPPING_WORDS,
						IMPORT_CLASSES_WORDS);
				importStuff(sql2o, path.resolve(SIMILARITY_CSV),
						SIMILARITY_TABLE, corpusId, IMPORT_SQL_ASSOCIATION,
						IMPORT_MAPPING_ASSOCIATION, IMPORT_CLASSES_ASSOCIATION);
				importStuff(sql2o, path.resolve(PPMI_CSV), PPMI_TABLE, corpusId,
						IMPORT_SQL_ASSOCIATION, IMPORT_MAPPING_ASSOCIATION,
						IMPORT_CLASSES_ASSOCIATION);
				importStuff(sql2o, path.resolve(CHI_CSV), CHI_TABLE, corpusId,
						IMPORT_SQL_ASSOCIATION, IMPORT_MAPPING_ASSOCIATION,
						IMPORT_CLASSES_ASSOCIATION);
				importStuff(sql2o, path.resolve(FREQUENCY_CSV), FREQUENCY_TABLE,
						corpusId, IMPORT_SQL_FREQUENCY,
						IMPORT_MAPPING_FREQUENCY, IMPORT_CLASSES_FREQUENCY);
			}
		}
	}

	public static void initializeTables(final Sql2o sql2o) {
		try (Connection con = sql2o.open()) {
			con.createQuery("CREATE SCHEMA " + SCHEMA).executeUpdate();
			con.createQuery("CREATE TABLE " + CORPORA
					+ " (id SERIAL, corpus TEXT, PRIMARY KEY(id) );")
					.executeUpdate(); //TODO: add descriptions/tooltipps? hardcoded atm
			con.createQuery("CREATE TABLE " + WORDIDS_TABLE
					+ " (corpus INTEGER, word TEXT, id INTEGER, PRIMARY KEY(corpus, word, id) );")
					.executeUpdate();
			final String assocTable = " (corpus INTEGER, word1 INTEGER, word2 INTEGER, year SMALLINT, association REAL, PRIMARY KEY(word1, year, corpus, word2) );";
			con.createQuery("CREATE TABLE " + SIMILARITY_TABLE + assocTable)
					.executeUpdate()
					.createQuery("CREATE INDEX word2_index ON "
							+ SIMILARITY_TABLE + " (word2,year,corpus);")
					.executeUpdate();
			con.createQuery("CREATE TABLE " + PPMI_TABLE + assocTable)
					.executeUpdate();
			con.createQuery("CREATE TABLE " + CHI_TABLE + assocTable)
			.executeUpdate();
			con.createQuery("CREATE TABLE " + FREQUENCY_TABLE
					+ " (corpus INTEGER, word INTEGER, year SMALLINT, frequency REAL, PRIMARY KEY(word, year, corpus));")
					.executeUpdate();
		}
	}

	private final Sql2o sql2o;

	public final Map<String, Corpus> corpora = new HashMap<>();

	public DatabaseService(final Sql2o sql2o)
			throws DatabaseUnitException, Exception {
		this.sql2o = sql2o;
		initializeMapping();
	}

	public List<String> getMostSimilarWordsInYear(final String corpusName,
			final String word, final Integer year, final int limit) {
		final Corpus corpus = corpora.get(corpusName);
		final List<String> words = new ArrayList<>();
		if (!corpus.hasMappingFor(word))
			return words;
		final Integer wordId = corpus.getIdFor(word);
		final String sql = MOST_SIMILAR_QUERY;
		try (Connection con = sql2o.open()) {
			for (final IDAndID ids : con.createQuery(sql)
					.addParameter("givenWord", wordId)
					.addParameter("corpus", corpus.getId())
					.addParameter("year", year).addParameter("limit", limit)
					.executeAndFetch(IDAndID.class)) {
				final Integer newWordId = ids.WORD1 == wordId ? ids.WORD2
						: ids.WORD1;
				words.add(corpus.getIdFor(newWordId));
			}
			return words;
		}
	}

	public void getTables() {
		final String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='JEDISEM'";
		try (Connection con = sql2o.open()) {
			final List<String> mostSimilar = con.createQuery(sql)
					.executeScalarList(String.class);
			System.out.println(mostSimilar);
		}
	}

	public List<String> getTopContextWordsInYear(final String corpusName,
			String table, final String word, final Integer year, final int limit) {
		final Corpus corpus = corpora.get(corpusName);
		final List<String> words = new ArrayList<>();
		if (!corpus.hasMappingFor(word))
			return words;
		final Integer givenWordId = corpus.getIdFor(word);
		final String sql = String.format(TOP_CONTEXT_QUERY, table);
		try (Connection con = sql2o.open()) {
			for (final Integer wordId : con.createQuery(sql)
					.addParameter("givenWord", givenWordId)
					.addParameter("corpus", corpus.getId())
					.addParameter("year", year).addParameter("limit", limit)
					.executeScalarList(Integer.class))
				words.add(corpus.getIdFor(wordId));
			return words;
		}
	}

	List<YearAndValue> getYearAndAssociation(final int corpus,
			final String tableName, final boolean isContextQuery, int word1Id,
			int word2Id) throws Exception {
		//similarity data is symmetric, only half/triangle of it needs to be stored
		if (!isContextQuery && (word1Id > word2Id)) {
			final int tmp = word1Id;
			word1Id = word2Id;
			word2Id = tmp;
		}
		final String sql = String.format(SIMILARITY_QUERY, tableName);
		try (Connection con = sql2o.open()) {
			final List<YearAndValue> mostSimilar = con.createQuery(sql)
					.addParameter("word1", word1Id)
					.addParameter("word2", word2Id)
					.addParameter("corpus", corpus)
					.executeAndFetch(YearAndValue.class);
			return mostSimilar;
		}
	}

	public List<YearAndValue> getYearAndAssociation(final String corpusName,
			final String tableName, final boolean isContextQuery,
			final String word1, final String word2) throws Exception {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word1, word2))
			return new ArrayList<>();
		return getYearAndAssociation(corpus.getId(), tableName, isContextQuery,
				corpus.getIdFor(word1), corpus.getIdFor(word2));
	}

	List<YearAndValue> getYearAndFrequency(final int corpus, final int wordId)
			throws Exception {
		try (Connection con = sql2o.open()) {
			return con.createQuery(FREQUENCY_QUERY)
					.addParameter("corpus", corpus).addParameter("word", wordId)
					.executeAndFetch(YearAndValue.class);
		}
	}

	// Will be used for testing

	public List<YearAndValue> getYearAndFrequency(final String corpusName,
			final String word) throws Exception {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return new ArrayList<>();
		return getYearAndFrequency(corpus.getId(), corpus.getIdFor(word));
	}

	public List<Integer> getYears(final String corpusName, final String word)
			throws Exception {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return new ArrayList<>();
		final Integer wordId = corpus.getIdFor(word);
		try (Connection con = sql2o.open()) {
			return con.createQuery(YEARS_QUERY).addParameter("word", wordId)
					.addParameter("corpus", corpus.getId())
					.executeScalarList(Integer.class);
		}
	}

	private void initializeMapping() {
		try (Connection con = sql2o.open()) {
			for (final WordAndID corpusAndId : con
					.createQuery("SELECT corpus as word, id FROM " + CORPORA)
					.executeAndFetch(WordAndID.class)) {
				final Corpus corpus = new Corpus(corpusAndId.id,
						con.createQuery("SELECT word,id FROM " + WORDIDS_TABLE
								+ " WHERE corpus=:corpus")
								.addParameter("corpus", corpusAndId.id)
								.executeAndFetch(WordAndID.class));
				corpora.put(corpusAndId.word, corpus);
			}
		}
	}

	/**
	 * Intended for testing
	 */
	public void dropAll() {
		try (Connection con = sql2o.open()) {
			con.createQuery("DROP SCHEMA " + SCHEMA + " CASCADE")
					.executeUpdate();
		}

	}

}
