package database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dbunit.DatabaseUnitException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Similarity data is symmetrical and has no ordering in its queries, while
 * cooccurrence is asymmetrical!
 * 
 * @author hellrich
 *
 */

public class DatabaseService {
	public static final String TEST_SIMILARITY = "JEDISEM.TEST_SIMILARITY";
	private static final String TEST_WORDIDS = "JEDISEM.TEST_WORD_IDS";
	private static final String DIRECTED_SIMILARITY_QUERY = "SELECT year, association FROM %s WHERE (word1=:word1 AND word2=:word2) ORDER BY year ASC";
	private static final String UNDIRECTED_SIMILARITY_QUERY = "SELECT year, association FROM %s WHERE (word1=:word1 AND word2=:word2) OR (word1=:word2 AND word2=:word1) ORDER BY year ASC";
	private static final String YEARS_QUERY = "SELECT DISTINCT year FROM %s WHERE word1=:word OR word2=:word ORDER BY year";
	private static final String MOST_SIMILAR_QUERY = "SELECT word1, word2 FROM %s WHERE (word1=:givenWord OR word2=:givenWord) AND year=:year ORDER BY association DESC LIMIT :limit";
	private static final String TOP_CONTEXT_QUERY = "SELECT word2 FROM %s WHERE word1=:givenWord AND year=:year ORDER BY association DESC LIMIT :limit";
	public static final String TEST_PPMI = "JEDISEM.TEST_PPMI";

	private final Sql2o sql2o;
	private final BiMap<String, Integer> word2IdMapping = HashBiMap.create();

	public DatabaseService(Sql2o sql2o) throws DatabaseUnitException, Exception {
		this.sql2o = sql2o;
		readDemo();
		initializeMapping();
	}

	private void initializeMapping() {
		String sql = "SELECT word,id FROM " + TEST_WORDIDS;
		try (Connection con = sql2o.open()) {
			for (WordAndID wordAndId : con.createQuery(sql).executeAndFetch(
					WordAndID.class)) {
				word2IdMapping.put(wordAndId.word, wordAndId.id);
			}
		}
	}

	public void getTables() {
		String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='JEDISEM'";
		try (Connection con = sql2o.open()) {
			List<String> mostSimilar = con.createQuery(sql).executeScalarList(
					String.class);
			System.out.println(mostSimilar);
		}
	}

	public List<YearAndAssociation> getYearAndAssociation(String table,
			boolean directed, String word1, String word2) throws Exception {
		return getYearAndAssociation(table, directed,
				word2IdMapping.get(word1), word2IdMapping.get(word2));
	}

	public List<YearAndAssociation> getYearAndAssociation(String table,
			boolean directed, int word1Id, int word2Id) throws Exception {
		String sql = directed ? String.format(DIRECTED_SIMILARITY_QUERY, table)
				: String.format(UNDIRECTED_SIMILARITY_QUERY, table);
		try (Connection con = sql2o.open()) {
			List<YearAndAssociation> mostSimilar = con.createQuery(sql)
					.addParameter("word1", word1Id)
					.addParameter("word2", word2Id)
					.executeAndFetch(YearAndAssociation.class);
			return mostSimilar;
		}
	}

	public List<Integer> getYears(String word) throws Exception {
		int wordId = word2IdMapping.get(word);
		String sql = String.format(YEARS_QUERY, TEST_SIMILARITY);
		try (Connection con = sql2o.open()) {
			return con.createQuery(sql).addParameter("word", wordId)
					.executeScalarList(Integer.class);
		}
	}

	public List<String> getMostSimilarWordsInYear(String givenWord,
			Integer year, int limit) {
		int givenWordId = word2IdMapping.get(givenWord);
		List<String> words = new ArrayList<>();
		String sql = String.format(MOST_SIMILAR_QUERY, TEST_SIMILARITY);
		try (Connection con = sql2o.open()) {
			for (IDAndID wordId : con.createQuery(sql)
					.addParameter("givenWord", givenWordId)
					.addParameter("year", year).addParameter("limit", limit)
					.executeAndFetch(IDAndID.class)) {
				int newWordId = wordId.WORD1 == givenWordId ? wordId.WORD2
						: wordId.WORD1;
				words.add(word2IdMapping.inverse().get(newWordId));
			}
			return words;
		}
	}

	public List<String> getTopContextWordsInYear(String table,
			String givenWord, Integer year, int limit) {
		int givenWordId = word2IdMapping.get(givenWord);
		List<String> words = new ArrayList<>();
		String sql = String.format(TOP_CONTEXT_QUERY, table);
		try (Connection con = sql2o.open()) {
			for (Integer wordId : con.createQuery(sql)
					.addParameter("givenWord", givenWordId)
					.addParameter("year", year).addParameter("limit", limit)
					.executeScalarList(Integer.class)) {
				words.add(word2IdMapping.inverse().get(wordId));
			}
			return words;
		}
	}

	//Will be used for testing
	void readDemo() throws Exception {
		try (Connection con = sql2o.open()) {
			con.createQuery("CREATE SCHEMA jedisem").executeUpdate();
			//TODO: think about good indexes
			con.createQuery(
					"CREATE TABLE "
							+ TEST_WORDIDS
							+ " (word TEXT, id INTEGER, PRIMARY KEY(word,id) );")
					.executeUpdate();
			String assocTable = " (word1 INTEGER, word2 INTEGER, year SMALLINT, association REAL, PRIMARY KEY(word1, word2, year) );";
			con.createQuery("CREATE TABLE " + TEST_SIMILARITY + assocTable)
					.executeUpdate();
			con.createQuery("CREATE TABLE " + TEST_PPMI + assocTable)
					.executeUpdate();
		}

		//ID mapping
		try (Connection con = sql2o.beginTransaction()) {
			org.sql2o.Query query = con.createQuery("INSERT INTO "
					+ TEST_WORDIDS + " (word, id) VALUES (:word, :id)");
			for (String[] x : Files
					.lines(Paths.get("src/test/resources/WORDIDS.csv"))
					.map(x -> x.split(",")).collect(Collectors.toList()))
				query.addParameter("word", x[0]).addParameter("id", x[1])
						.addToBatch();
			query.executeBatch(); // executes entire batch
			con.commit(); // remember to call commit(), else sql2o will automatically rollback.
		}

		importAssociation(TEST_SIMILARITY, "src/test/resources/SIMILARITY.csv");
		importAssociation(TEST_PPMI, "src/test/resources/PPMI.csv");

	}

	private void importAssociation(String tableName, String fileName)
			throws IOException {
		try (Connection con = sql2o.beginTransaction()) {
			org.sql2o.Query query = con
					.createQuery("INSERT INTO "
							+ tableName
							+ " (word1, word2, year, association) VALUES (:word1, :word2, :year, :association)");
			for (String[] x : Files.lines(Paths.get(fileName))
					.map(x -> x.split(",")).collect(Collectors.toList()))
				query.addParameter("word1", x[0]).addParameter("word2", x[1])
						.addParameter("year", x[2])
						.addParameter("association", x[3]).addToBatch();
			query.executeBatch(); // executes entire batch
			con.commit(); // remember to call commit(), else sql2o will automatically rollback.
		}
	}
}
