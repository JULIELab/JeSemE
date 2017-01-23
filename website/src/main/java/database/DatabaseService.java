package database;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.dbunit.DatabaseUnitException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

/**
 * Databases with similarity results use inherently ordered keys (alphabetical
 * order of words, i.e. a only as first key, x only as second) to faciliate more
 * efficient queries. Only traingle instead of full crossproduct of similarities
 * is stored.
 * 
 * This is (due to scope at text/sentence endings) not applicable to
 * cooccurrence type data, there full data and no special ordering.
 * 
 * @author hellrich
 *
 */

public class DatabaseService {
	private static final String SIMILARITY_QUERY = "SELECT year, similarity FROM %s WHERE word1=:word1 AND word2=:word2 ORDER BY year ASC";
	private static final String YEARS_QUERY = "SELECT DISTINCT year FROM %s WHERE word1=:word OR word2=:word ORDER BY year";
	private static final String MOST_SIMILAR_QUERY = "SELECT word1 FROM %s WHERE word2=:givenWord AND year=:year ORDER BY similarity DESC LIMIT :limit";
	private final Sql2o sql2o;

	public DatabaseService(Sql2o sql2o) throws DatabaseUnitException, Exception {
		this.sql2o = sql2o;
		readDemo();
	}

	public List<YearAndSimilarity> getYearAndSimilarity(String word1,
			String word2) throws Exception {
		String sql = String.format(SIMILARITY_QUERY, "test");

		try (Connection con = sql2o.open()) {
			List<YearAndSimilarity> mostSimilar = con.createQuery(sql)
					.addParameter("word1", word1).addParameter("word2", word2)
					.executeAndFetch(YearAndSimilarity.class);
			return mostSimilar;
		}
	}

	public List<Integer> getYears(String word) throws Exception {
		String sql = String.format(YEARS_QUERY, "test");
		try (Connection con = sql2o.open()) {
			return con.createQuery(sql).addParameter("word", word)
					.executeScalarList(Integer.class);
		}
	}

	public List<String> getMostSimilarWordsInYear(String givenWord,
			Integer year, int limit) {
		String sql = String.format(MOST_SIMILAR_QUERY, "test");
		try (Connection con = sql2o.open()) {
			return con.createQuery(sql).addParameter("givenWord", givenWord)
					.addParameter("year", year).addParameter("limit", limit)
					.executeScalarList(String.class);
		}
	}

	void readDemo() throws Exception {
		String create = "CREATE TABLE test (word1 TEXT, word2 TEXT, year SMALLINT, similarity REAL, PRIMARY KEY(word1, word2, year) );";

		try (Connection con = sql2o.open()) {
			con.createQuery(create).executeUpdate();
		}

		String insert = "INSERT INTO test (word1, word2, year, similarity) VALUES (:word1, :word2, :year, :similarity)";
		try (Connection con = sql2o.beginTransaction()) {
			org.sql2o.Query query = con.createQuery(insert);

			for (String[] x : Files
					.lines(Paths.get("src/test/resources/CrossSimilarity.demo"))
					.map(x -> x.split(" ")).collect(Collectors.toList()))
				query.addParameter("word1", x[0]).addParameter("word2", x[1])
						.addParameter("year", x[2])
						.addParameter("similarity", x[3]).addToBatch();

			query.executeBatch(); // executes entire batch
			con.commit(); // remember to call commit(), else sql2o will automatically rollback.
		}
	}
}
