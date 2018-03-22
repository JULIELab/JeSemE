package de.julielab.jeseme.database;

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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.julielab.jeseme.configuration.Configuration;
import de.julielab.jeseme.database.corpus.Corpus;
import de.julielab.jeseme.database.corpus.DTAMapper;
import de.julielab.jeseme.database.corpus.DummyMapper;
import de.julielab.jeseme.database.corpus.LowerCaseMapper;
import de.julielab.jeseme.database.corpus.WordMapper;
import de.julielab.jeseme.database.importer.AssociationImporter;
import de.julielab.jeseme.database.importer.CLOBImporter;
import de.julielab.jeseme.database.importer.FrequencyImporter;
import de.julielab.jeseme.database.importer.Importer;
import de.julielab.jeseme.database.importer.WordImporter;
import de.julielab.jeseme.embeddings.Embedding;

/**
 *
 * @author hellrich
 *
 */

public class DatabaseService {
	private static final String SCHEMA = "JESEME_V2_1"; //TODO update if reprocessed
	private static final String CORPORA = SCHEMA + ".TABLES";
	public static final String SIMILARITY_TABLE = SCHEMA + ".SIMILARITY";
	public static final String EMOTION_TABLE = SCHEMA + ".EMOTION";
	private static final String WORDIDS_TABLE = SCHEMA + ".WORDIDS";
	public static final String PPMI_TABLE = SCHEMA + ".PPMI";
	public static final String EMBEDDING_TABLE = SCHEMA + ".EMBEDDING";
	public static final String CHI_TABLE = SCHEMA + ".CHI";
	private static final String FREQUENCY_TABLE = SCHEMA + ".FREQUENCY";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DatabaseService.class);

	private static final String ASSOCIATION_QUERY = "SELECT year, association AS value FROM "
			+ "%s"
			+ " WHERE corpus=:corpus AND (word1=:word1 AND word2=:word2) ORDER BY year ASC";

	private static final String YEAR_QUERY_FRAGMENT = "SELECT year FROM "
			+ FREQUENCY_TABLE
			+ " WHERE corpus=:corpus AND word=:word ORDER BY year";

	private static final String FIRST_YEAR_QUERY = YEAR_QUERY_FRAGMENT
			+ " ASC LIMIT 1";
	private static final String LAST_YEAR_QUERY = YEAR_QUERY_FRAGMENT
			+ " DESC LIMIT 1";
	private static final String TOP_CONTEXT_QUERY = "SELECT word2 FROM %s WHERE corpus=:corpus AND word1=:givenWord AND "
			+ "year=:year ORDER BY association DESC LIMIT :limit";

	private static final String YEAR_AND_STUFF_QUERY_TEMPLATE = "SELECT year, <WHAT> AS value FROM <TABLE> "
			+ "WHERE corpus=:corpus AND word=:word ORDER BY year ASC";
	private static final String FREQUENCY_QUERY = YEAR_AND_STUFF_QUERY_TEMPLATE
			.replace("<WHAT>", "frequency").replace("<TABLE>", FREQUENCY_TABLE);
	private static final String CLOB_QUERY = YEAR_AND_STUFF_QUERY_TEMPLATE
			.replace("<WHAT>", "clob");
	private static final String EMBEDDING_QUERY = CLOB_QUERY.replace("<TABLE>",
			EMBEDDING_TABLE);
	private static final String EMOTION_QUERY = CLOB_QUERY.replace("<TABLE>",
			EMOTION_TABLE);

	private static Integer getYear(final String query, final Sql2o sql2o,
			final Map<String, Corpus> corpora, final String word,
			final String corpusName) {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return null;
		final Integer wordId = corpus.getIdFor(word);
		try (Connection con = sql2o.open()) {
			return con.createQuery(query).addParameter("word", wordId)
					.addParameter("corpus", corpus.getId())
					.executeScalar(Integer.class);
		}
	}

	/**
	 * Static to avoid initializeMapping
	 */
	public static void importTables(final Configuration config,
			final Sql2o sql2o) throws Exception {

		for (final de.julielab.jeseme.configuration.Corpus corpus : config
				.getCorpora()) {
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
				new CLOBImporter(sql2o, corpusId, EMBEDDING_TABLE)
						.importStuff(path.resolve(Importer.EMBEDDING_CSV));
				new CLOBImporter(sql2o, corpusId, EMOTION_TABLE)
						.importStuff(path.resolve(Importer.EMOTION_CSV));
				LOGGER.info("Finished import");
			}
		}
	}

	/**
	 * Static to avoid initializeMapping
	 */
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
					.executeUpdate();
			con.createQuery("CREATE TABLE " + PPMI_TABLE + assocTable)
					.executeUpdate();
			con.createQuery("CREATE TABLE " + CHI_TABLE + assocTable)
					.executeUpdate();
			con.createQuery("CREATE TABLE " + FREQUENCY_TABLE
					+ " (corpus INTEGER, word INTEGER, year SMALLINT, frequency REAL, PRIMARY KEY(word, year, corpus));")
					.executeUpdate();
			final String clobTable = " (corpus INTEGER, word INTEGER, year SMALLINT, clob TEXT, PRIMARY KEY(word, year, corpus));";
			con.createQuery("CREATE TABLE " + EMBEDDING_TABLE + clobTable)
					.executeUpdate();
			con.createQuery("CREATE TABLE " + EMOTION_TABLE + clobTable)
					.executeUpdate();
		}
	}

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
		final Corpus c = corpora.get(corpus);
		if (c != null)
			return c.getUrl(word);
		return null;
	}

	public String getCorpusName(final String corpus) {
		final Corpus c = corpora.get(corpus);
		if (c != null)
			return c.getFullName();
		return null;
	}

	public String getCorpusNote(final String corpus) {
		final Corpus c = corpora.get(corpus);
		if (c != null)
			return c.getNote();
		return null;
	}

	List<YearAndString> getEmbedding(final int corpus, final int wordId)
			throws Exception {
		try (Connection con = sql2o.open()) {
			return con.createQuery(EMBEDDING_QUERY)
					.addParameter("corpus", corpus).addParameter("word", wordId)
					.executeAndFetch(YearAndString.class);
		}
	}

	public Map<Integer, Embedding> getEmbedding(final String corpusName,
			final String word) throws Exception {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return Maps.newHashMap();
		return getEmbedding(corpus.getId(), corpus.getIdFor(word)).stream()
				.collect(Collectors.toMap(yas -> yas.year,
						yas -> new Embedding(yas.value)));
	}

	List<YearAndString> getEmotion(final int corpus, final int wordId)
			throws Exception {
		try (Connection con = sql2o.open()) {
			return con.createQuery(EMOTION_QUERY).addParameter("corpus", corpus)
					.addParameter("word", wordId)
					.executeAndFetch(YearAndString.class);
		}
	}

	public Map<String, List<YearAndValue>> getEmotion(final String corpusName,
			final String word) throws Exception {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return Maps.newHashMap();

		final String[] emotions = "valence,arousal,dominance".split(",");
		final Map<String, List<YearAndValue>> emotion2yav = new HashMap<>();
		for (final String s : emotions)
			emotion2yav.put(s, new ArrayList<>());
		getEmotion(corpus.getId(), corpus.getIdFor(word)).forEach(yas -> {
			final String[] vad = yas.value.split(" ");
			for (int i = 0; i < emotions.length; ++i)
				emotion2yav.get(emotions[i])
						.add(new YearAndValue(yas.year, Float.valueOf(vad[i])));
		});
		return emotion2yav;
	}

	public Integer getFirstYear(final String corpusName, final String word)
			throws Exception {
		return getYear(FIRST_YEAR_QUERY, sql2o, corpora, word, corpusName);
	}

	public Integer getLastYear(final String corpusName, final String word)
			throws Exception {
		return getYear(LAST_YEAR_QUERY, sql2o, corpora, word, corpusName);
	}

	public List<YearAndValue> getSimilarity(final String corpusName,
			final String word1, final String word2) throws Exception {
		final Map<Integer, Embedding> embeddings1 = getEmbedding(corpusName,
				word1);
		final Map<Integer, Embedding> embeddings2 = getEmbedding(corpusName,
				word2);
		return Sets.union(embeddings1.keySet(), embeddings2.keySet()).stream()
				.sorted().map(year -> {
					if (embeddings1.containsKey(year)
							&& embeddings2.containsKey(year))
						return new YearAndValue(year, (float) embeddings1
								.get(year).similarity(embeddings2.get(year)));
					else
						return new YearAndValue(year, 0f);
				}).collect(Collectors.toList());
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
			final String tableName, final int word1Id, final int word2Id)
			throws Exception {
		final String sql = String.format(ASSOCIATION_QUERY, tableName);
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
			final String tableName, final String word1, final String word2)
			throws Exception {
		final Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word1, word2))
			return new ArrayList<>();
		return getYearAndAssociation(corpus.getId(), tableName,
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
				final List<de.julielab.jeseme.configuration.Corpus> corpusConfigs = config
						.getCorpora().stream()
						.filter(x -> x.getName().equals(corpusAndId.word))
						.collect(Collectors.toList());
				if (corpusConfigs.size() != 1)
					throw new IllegalArgumentException(
							"Configuration and database do not match for "
									+ corpusAndId.word);
				final de.julielab.jeseme.configuration.CorpusInfo info = corpusConfigs
						.get(0).getInfo();
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
		}catch(Exception e){
			LOGGER.error("Exception:", e);	
		}
	}

	public boolean knowCorpus(final String corpus) {
		return corpora.containsKey(corpus);
	}

	public boolean wordInCorpus(final String word, final String corpus) {
		return corpora.containsKey(corpus)
				&& corpora.get(corpus).hasMappingFor(word);
	}
}
