package database;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.dbunit.DatabaseUnitException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Query {
	private final Sql2o sql2o;

	public Query(Sql2o sql2o) throws DatabaseUnitException, Exception {
		this.sql2o = sql2o;
		readDemo();
	}

	public List<Float> getSimilarity(String word1, String word2) throws Exception {		
		String sql = "SELECT similarity FROM test WHERE word1=:word1 AND word2=:word2 ORDER BY year ASC";
		
		try (Connection con = sql2o.open()) {
			List<Float> mostSimilar = con.createQuery(sql)
					.addParameter("word1", word1)
					.addParameter("word2", word2)
					.executeScalarList(Float.class);
			return mostSimilar;
		}
	}
	
	public List<YearAndSimilarity> getYearAndSimilarity(String word1, String word2) throws Exception {		
		String sql = "SELECT year, similarity FROM test WHERE word1=:word1 AND word2=:word2 ORDER BY year ASC";
		
		try (Connection con = sql2o.open()) {
			List<YearAndSimilarity> mostSimilar = con.createQuery(sql)
					.addParameter("word1", word1)
					.addParameter("word2", word2)
					.executeAndFetch(YearAndSimilarity.class);
			return mostSimilar;
		}
	}

	public List<Integer> getYears(String word) throws Exception {
		String sql = "SELECT DISTINCT year FROM test WHERE word1=:word1 OR word2=:word2 ORDER BY year";
		try (Connection con = sql2o.open()) {
			return con.createQuery(sql)
					.addParameter("word1", word)
					.addParameter("word2", word).executeScalarList(Integer.class);
		}
	}

	public List<String> getMostSimilarWordsInYear(String givenWord,
			Integer year, int limit) {


		String sql = "SELECT word1 FROM "+"test"+" WHERE word2=:givenWord AND year=:year ORDER BY similarity DESC LIMIT :limit";
		try (Connection con = sql2o.open()) {
			return con.createQuery(sql)
					.addParameter("givenWord", givenWord)
					.addParameter("year", year).addParameter("limit", limit).executeScalarList(String.class);
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
