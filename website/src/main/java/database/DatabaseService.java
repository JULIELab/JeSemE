package database;

import java.io.File;
import java.io.FileInputStream;
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
import org.yaml.snakeyaml.Yaml;

/**
 * 
 * @author hellrich
 *
 */

public class DatabaseService {
	private static final String TEST_PATH = "src/test/resources/";

	private static final String FREQUENCY_CSV = "FREQUENCY.csv";
	private static final String PPMI_CSV = "PPMI.csv";
	private static final String SIMILARITY_CSV = "SIMILARITY.csv";
	private static final String WORDIDS_CSV = "WORDIDS.csv";

	private static final String SCHEMA = "JEDISEM";
	private static final String CORPORA = SCHEMA + ".TABLES";
	public static final String SIMILARITY_TABLE = SCHEMA + ".SIMILARITY";
	private static final String WORDIDS_TABLE = SCHEMA + ".WORDIDS";
	public static final String PPMI_TABLE = SCHEMA + ".PPMI";
	private static final String FREQUENCY_TABLE = SCHEMA + ".FREQUENCY";

	private static final String SIMILARITY_QUERY = "SELECT year, association AS value FROM "
			+ "%s"
			+ " WHERE corpus=:corpus AND (word1=:word1 AND word2=:word2) ORDER BY year ASC";
	private static final String YEARS_QUERY = "SELECT DISTINCT year FROM "
			+ SIMILARITY_TABLE
			+ " WHERE corpus=:corpus AND (word1=:word OR word2=:word) ORDER BY year";
	private static final String MOST_SIMILAR_QUERY = "SELECT word1, word2 FROM "
			+ SIMILARITY_TABLE
			+ " WHERE corpus=:corpus AND (word1=:givenWord OR word2=:givenWord) AND year=:year ORDER BY association DESC LIMIT :limit";
	private static final String TOP_CONTEXT_QUERY = "SELECT word2 FROM "
			+ PPMI_TABLE
			+ " WHERE corpus=:corpus AND word1=:givenWord AND year=:year ORDER BY association DESC LIMIT :limit";
	private static final String FREQUENCY_QUERY = "SELECT year, frequency AS value FROM "
			+ FREQUENCY_TABLE
			+ " WHERE corpus=:corpus AND word=:word ORDER BY year ASC";

	private final Sql2o sql2o;
	public final Map<String, Corpus> corpora = new HashMap<>();

	public DatabaseService(Sql2o sql2o) throws DatabaseUnitException, Exception {
		this(sql2o, TEST_PATH);
	}

	public DatabaseService(Sql2o sql2o, String path)
			throws DatabaseUnitException, Exception {
		this.sql2o = sql2o;
		readDemo(path);
		initializeMapping();
	}

	private void initializeMapping() {
		try (Connection con = sql2o.open()) {
			for (WordAndID corpusAndId : con
					.createQuery("SELECT corpus as word, id FROM " + CORPORA)
					.executeAndFetch(WordAndID.class)) {
				Corpus corpus = new Corpus(corpusAndId.id,
						con.createQuery("SELECT word,id FROM " + WORDIDS_TABLE
								+ " WHERE corpus=:corpus")
								.addParameter("corpus", corpusAndId.id)
								.executeAndFetch(WordAndID.class));
				corpora.put(corpusAndId.word, corpus);
			}
		}
	}

	public void getTables() {
		String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='JEDISEM'";
		try (Connection con = sql2o.open()) {
			List<String> mostSimilar = con.createQuery(sql)
					.executeScalarList(String.class);
			System.out.println(mostSimilar);
		}
	}

	public List<YearAndValue> getYearAndAssociation(String corpusName,
			String tableName, boolean isContextQuery, String word1,
			String word2) throws Exception {
		Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word1, word2))
			return new ArrayList<>();
		return getYearAndAssociation(corpus.getId(), tableName, isContextQuery,
				corpus.getIdFor(word1), corpus.getIdFor(word2));
	}

	List<YearAndValue> getYearAndAssociation(int corpus, String tableName,
			boolean isContextQuery, int word1Id, int word2Id) throws Exception {
		//similarity data is symmetric, only half/traingle of it needs to be stored
		if (!isContextQuery && word1Id > word2Id) {
			int tmp = word1Id;
			word1Id = word2Id;
			word2Id = tmp;
		}
		String sql = String.format(SIMILARITY_QUERY, tableName);
		try (Connection con = sql2o.open()) {
			List<YearAndValue> mostSimilar = con.createQuery(sql)
					.addParameter("word1", word1Id)
					.addParameter("word2", word2Id)
					.addParameter("corpus", corpus)
					.executeAndFetch(YearAndValue.class);
			return mostSimilar;
		}
	}

	public List<YearAndValue> getYearAndFrequency(String corpusName,
			String word) throws Exception {
		Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return new ArrayList<>();
		return getYearAndFrequency(corpus.getId(), corpus.getIdFor(word));
	}

	List<YearAndValue> getYearAndFrequency(int corpus, int wordId)
			throws Exception {
		try (Connection con = sql2o.open()) {
			return con.createQuery(FREQUENCY_QUERY)
					.addParameter("corpus", corpus).addParameter("word", wordId)
					.executeAndFetch(YearAndValue.class);
		}
	}

	public List<Integer> getYears(String corpusName, String word)
			throws Exception {
		Corpus corpus = corpora.get(corpusName);
		if (!corpus.hasMappingFor(word))
			return new ArrayList<>();
		Integer wordId = corpus.getIdFor(word);
		try (Connection con = sql2o.open()) {
			return con.createQuery(YEARS_QUERY).addParameter("word", wordId)
					.addParameter("corpus", corpus.getId())
					.executeScalarList(Integer.class);
		}
	}

	public List<String> getMostSimilarWordsInYear(String corpusName,
			String word, Integer year, int limit) {
		Corpus corpus = corpora.get(corpusName);
		List<String> words = new ArrayList<>();
		if (!corpus.hasMappingFor(word))
			return words;
		Integer wordId = corpus.getIdFor(word);
		String sql = MOST_SIMILAR_QUERY;
		try (Connection con = sql2o.open()) {
			for (IDAndID ids : con.createQuery(sql)
					.addParameter("givenWord", wordId)
					.addParameter("corpus", corpus.getId())
					.addParameter("year", year).addParameter("limit", limit)
					.executeAndFetch(IDAndID.class)) {
				Integer newWordId = ids.WORD1 == wordId ? ids.WORD2 : ids.WORD1;
				words.add(corpus.getIdFor(newWordId));
			}
			return words;
		}
	}

	public List<String> getTopContextWordsInYear(String corpusName, String word,
			Integer year, int limit) {
		Corpus corpus = corpora.get(corpusName);
		List<String> words = new ArrayList<>();
		if (!corpus.hasMappingFor(word))
			return words;
		Integer givenWordId = corpus.getIdFor(word);
		String sql = TOP_CONTEXT_QUERY;
		try (Connection con = sql2o.open()) {
			for (Integer wordId : con.createQuery(sql)
					.addParameter("givenWord", givenWordId)
					.addParameter("corpus", corpus.getId())
					.addParameter("year", year).addParameter("limit", limit)
					.executeScalarList(Integer.class)) {
				words.add(corpus.getIdFor(wordId));
			}
			return words;
		}
	}

	// Will be used for testing

	@SuppressWarnings("unchecked")
	void readDemo(String descriptorPath) throws Exception {
		makeTables();

		for (Map<String, String> table : (List<Map<String, String>>) new Yaml()
				.load(new FileInputStream(
						new File(descriptorPath, "tables.yaml")))) {
			String corpusName = table.get("name");
			Path path = Paths.get(descriptorPath, table.get("folder"));

			try (Connection con = sql2o.open()) {
				int corpus = (int) con
						.createQuery("INSERT INTO " + CORPORA
								+ " (corpus) VALUES (:corpus);", true)
						.addParameter("corpus", corpusName).executeUpdate()
						.getKey();

				importWords(WORDIDS_TABLE, corpus, path);
				importAssociation(SIMILARITY_TABLE, corpus,
						path.resolve(SIMILARITY_CSV));
				importAssociation(PPMI_TABLE, corpus, path.resolve(PPMI_CSV));
				importFrequency(FREQUENCY_TABLE, corpus,
						path.resolve(FREQUENCY_CSV));
			}
		}
	}

	private void makeTables() {
		try (Connection con = sql2o.open()) {
			con.createQuery("CREATE SCHEMA " + SCHEMA).executeUpdate();
			con.createQuery("CREATE TABLE " + CORPORA
					+ " (id SERIAL, corpus TEXT, PRIMARY KEY(id) );")
					.executeUpdate(); //TODO: add descriptions/tooltipps
			con.createQuery("CREATE TABLE " + WORDIDS_TABLE
					+ " (corpus INTEGER, word TEXT, id INTEGER, PRIMARY KEY(corpus, word, id) );")
					.executeUpdate();
			String assocTable = " (corpus INTEGER, word1 INTEGER, word2 INTEGER, year SMALLINT, association REAL, PRIMARY KEY(word1, year, corpus, word2) );";
			con.createQuery("CREATE TABLE " + SIMILARITY_TABLE + assocTable)
					.executeUpdate()
					.createQuery("CREATE INDEX word2_index ON "
							+ SIMILARITY_TABLE + " (word2,year,corpus);")
					.executeUpdate();
			con.createQuery("CREATE TABLE " + PPMI_TABLE + assocTable)
					.executeUpdate();
			con.createQuery("CREATE TABLE " + FREQUENCY_TABLE
					+ " (corpus INTEGER, word INTEGER, year SMALLINT, frequency REAL, PRIMARY KEY(word, year, corpus));")
					.executeUpdate();
		}
	}

	private void importWords(String tableName, Integer corpus, Path path)
			throws IOException {
		try (Connection con = sql2o.beginTransaction()) {
			org.sql2o.Query query = con.createQuery("INSERT INTO " + tableName
					+ " (corpus, word, id) VALUES (:corpus, :word, :id)");
			Files.lines(path.resolve(WORDIDS_CSV)).map(x -> x.split(","))
					.forEach(x -> query.addParameter("corpus", corpus)
							.addParameter("word", x[0]).addParameter("id", x[1])
							.addToBatch());
			query.executeBatch(); // executes entire batch
			con.commit(); // remember to call commit(), else sql2o will
							// automatically rollback.
		}

	}

	private void importFrequency(String tableName, Integer corpus, Path path)
			throws IOException {
		try (Connection con = sql2o.beginTransaction()) {
			org.sql2o.Query query = con.createQuery("INSERT INTO " + tableName
					+ " (corpus, word, year, frequency) VALUES (:corpus, :word, :year, :frequency)");
			Files.lines(path).map(x -> x.split(","))
					.forEach(x -> query.addParameter("corpus", corpus)
							.addParameter("word", x[0])
							.addParameter("year", x[1])
							.addParameter("frequency", x[2]).addToBatch());
			query.executeBatch(); // executes entire batch
			con.commit(); // remember to call commit(), else sql2o will
							// automatically rollback.
		}
	}

	private void importAssociation(String tableName, Integer corpus, Path path)
			throws IOException {
		try (Connection con = sql2o.beginTransaction()) {
			org.sql2o.Query query = con.createQuery("INSERT INTO " + tableName
					+ " (corpus, word1, word2, year, association) VALUES (:corpus, :word1, :word2, :year, :association)");
			Files.lines(path).map(x -> x.split(",")).forEach(x -> query
					.addParameter("corpus", corpus).addParameter("word1", x[0])
					.addParameter("word2", x[1]).addParameter("year", x[2])
					.addParameter("association", x[3]).addToBatch());

			query.executeBatch(); // executes entire batch
			con.commit(); // remember to call commit(), else sql2o will
							// automatically rollback.
		}
	}
}
