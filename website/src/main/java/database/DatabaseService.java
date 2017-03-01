package database;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbunit.DatabaseUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.google.common.collect.HashBasedTable;

import configuration.Configuration;
import database.corpus.Corpus;
import database.corpus.DTAMapper;
import database.corpus.DummyMapper;
import database.corpus.LowerCaseMapper;
import database.corpus.WordMapper;
import database.importer.AssociationImporter;
import database.importer.FrequencyImporter;
import database.importer.Importer;
import database.importer.WordImporter;

/**
 *
 * @author hellrich
 *
 */

public class DatabaseService {
	private static final String SCHEMA = "JEDISEM_V09";
	private static final String CORPORA = SCHEMA + ".TABLES";
	public static final String SIMILARITY_TABLE = SCHEMA + ".SIMILARITY";
	private static final String WORDIDS_TABLE = SCHEMA + ".WORDIDS";
	public static final String PPMI_TABLE = SCHEMA + ".PPMI";
	public static final String CHI_TABLE = SCHEMA + ".CHI";
	private static final String FREQUENCY_TABLE = SCHEMA + ".FREQUENCY";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DatabaseService.class);
	private static final String SIMILARITY_QUERY = "SELECT year, association AS value FROM "
			+ "%s"
			+ " WHERE corpus=:corpus AND (word1=:word1 AND word2=:word2) ORDER BY year ASC";

	private static final String YEAR_QUERY = "SELECT year FROM "
			+ FREQUENCY_TABLE
			+ " WHERE corpus=:corpus AND word=:word ORDER BY year";

	private static final String FIRST_YEAR_QUERY = YEAR_QUERY + " ASC LIMIT 1";
	private static final String LAST_YEAR_QUERY = YEAR_QUERY + " DESC LIMIT 1";
	private static final String MOST_SIMILAR_QUERY = "SELECT word1, word2 FROM "
			+ SIMILARITY_TABLE
			+ " WHERE corpus=:corpus AND (word1=:givenWord OR word2=:givenWord) AND year=:year ORDER BY association DESC LIMIT :limit";
	private static final String TOP_CONTEXT_QUERY = "SELECT word2 FROM %s WHERE corpus=:corpus AND word1=:givenWord AND year=:year ORDER BY association DESC LIMIT :limit";
	private static final String FREQUENCY_QUERY = "SELECT year, frequency AS value FROM "
			+ FREQUENCY_TABLE
			+ " WHERE corpus=:corpus AND word=:word ORDER BY year ASC";

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
				new WordImporter(sql2o, corpusId, WORDIDS_TABLE)
						.importStuff(path.resolve(Importer.WORDS_CSV));
				new AssociationImporter(sql2o, corpusId, SIMILARITY_TABLE)
						.importStuff(path.resolve(Importer.SIMILARITY_CSV));
				new AssociationImporter(sql2o, corpusId, PPMI_TABLE)
						.importStuff(path.resolve(Importer.PPMI_CSV));
				new AssociationImporter(sql2o, corpusId, CHI_TABLE)
						.importStuff(path.resolve(Importer.CHI_CSV));
				new FrequencyImporter(sql2o, corpusId, FREQUENCY_TABLE)
						.importStuff(path.resolve(Importer.FREQUENCY_CSV));

				LOGGER.info("Finished import");
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

	private final HashBasedTable<String, String, Integer> firstYearCache = HashBasedTable
			.create();

	private final HashBasedTable<String, String, Integer> lastYearCache = HashBasedTable
			.create();

	private final Sql2o sql2o;

	public final Map<String, Corpus> corpora = new HashMap<>();

	public DatabaseService(final Sql2o sql2o, final Configuration config)
			throws DatabaseUnitException, Exception {
		this.sql2o = sql2o;
		initializeMapping(config);
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

	public String getCorpusLink(final String corpus, final String word) {
		return corpora.get(corpus).getUrl(word);
	}

	public String getCorpusName(final String corpus) {
		return corpora.get(corpus).getFullName();
	}

	public String getCorpusNote(final String corpus) {
		return corpora.get(corpus).getNote();
	}

	public Integer getFirstYear(final String corpusName, final String word)
			throws Exception {
		if (!firstYearCache.contains(corpusName, word)) {
			final Corpus corpus = corpora.get(corpusName);
			if (!corpus.hasMappingFor(word))
				firstYearCache.put(corpusName, word, null);
			final Integer wordId = corpus.getIdFor(word);
			try (Connection con = sql2o.open()) {
				final Integer i = con.createQuery(FIRST_YEAR_QUERY)
						.addParameter("word", wordId)
						.addParameter("corpus", corpus.getId())
						.executeScalar(Integer.class);
				firstYearCache.put(corpusName, word, i);
			}
		}
		return firstYearCache.get(corpusName, word);
	}

	public Integer getLastYear(final String corpusName, final String word)
			throws Exception {
		if (!lastYearCache.contains(corpusName, word)) {
			final Corpus corpus = corpora.get(corpusName);
			if (!corpus.hasMappingFor(word))
				lastYearCache.put(corpusName, word, null);
			final Integer wordId = corpus.getIdFor(word);
			try (Connection con = sql2o.open()) {
				final Integer i = con.createQuery(LAST_YEAR_QUERY)
						.addParameter("word", wordId)
						.addParameter("corpus", corpus.getId())
						.executeScalar(Integer.class);
				lastYearCache.put(corpusName, word, i);
			}
		}
		return lastYearCache.get(corpusName, word);
	}

	// Will be used for testing

	public List<String> getMostSimilarWordsInYear(final String corpusName,
			final String word, final Integer year, final int limit) {
		final Corpus corpus = corpora.get(corpusName);
		final List<String> words = new ArrayList<>();
		if (!corpus.hasMappingFor(word) || (year == null))
			return words;
		final Integer wordId = corpus.getIdFor(word);
		final String sql = MOST_SIMILAR_QUERY;

		try (Connection con = sql2o.open()) {
			for (final IDAndID ids : con.createQuery(sql)
					.addParameter("givenWord", wordId)
					.addParameter("corpus", corpus.getId())
					.addParameter("year", year).addParameter("limit", limit)
					.executeAndFetch(IDAndID.class)) {
				final Integer newWordId = ids.WORD1.equals(wordId) ? ids.WORD2
						: ids.WORD1;
				words.add(corpus.getStringFor(newWordId));
			}
			return words;
		}
	}

	public List<String> getTopContextWordsInYear(final String corpusName,
			final String table, final String word, final Integer year,
			final int limit) {
		final Corpus corpus = corpora.get(corpusName);
		final List<String> words = new ArrayList<>();
		if (!corpus.hasMappingFor(word) || (year == null))
			return words;
		final Integer givenWordId = corpus.getIdFor(word);
		final String sql = String.format(TOP_CONTEXT_QUERY, table);
		try (Connection con = sql2o.open()) {
			for (final Integer wordId : con.createQuery(sql)
					.addParameter("givenWord", givenWordId)
					.addParameter("corpus", corpus.getId())
					.addParameter("year", year).addParameter("limit", limit)
					.executeScalarList(Integer.class))
				words.add(corpus.getStringFor(wordId));
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

	public List<YearAndValue> getYearAndFrequency(final String corpusName,
			final String word) throws Exception {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return new ArrayList<>();
		return getYearAndFrequency(corpus.getId(), corpus.getIdFor(word));
	}

	private void initializeMapping(final Configuration config)
			throws IOException {
		try (Connection con = sql2o.open()) {
			for (final WordAndID corpusAndId : con
					.createQuery("SELECT corpus as word, id FROM " + CORPORA)
					.executeAndFetch(WordAndID.class)) {
				WordMapper mapper = null;
				final List<configuration.Corpus> corpusConfigs = config
						.getCorpora().stream()
						.filter(x -> x.getName().equals(corpusAndId.word))
						.collect(Collectors.toList());
				if (corpusConfigs.size() != 1)
					throw new IllegalArgumentException(
							"Configuration and database do not match for "
									+ corpusAndId.word);
				final configuration.CorpusInfo info = corpusConfigs.get(0)
						.getInfo();
				final String pathName = info.getMappingPath();
				final boolean lowercase = info.getLowercase();
				if (pathName == null)
					if (lowercase)
						mapper = new LowerCaseMapper();
					else
						mapper = new DummyMapper();
				else
					mapper = new DTAMapper(Paths.get(pathName), lowercase);

				final Corpus corpus = new Corpus(corpusAndId.id,
						con.createQuery("SELECT word,id FROM " + WORDIDS_TABLE
								+ " WHERE corpus=:corpus")
								.addParameter("corpus", corpusAndId.id)
								.executeAndFetch(WordAndID.class),
						mapper, info.getFullName(), info.getNote(),
						info.getUrl(), info.getInsertInUrl());
				corpora.put(corpusAndId.word, corpus);
			}
		}
	}

	public boolean wordInCorpus(final String word, final String corpus) {
		return corpora.containsKey(corpus)
				&& corpora.get(corpus).hasMappingFor(word);
	}
}
