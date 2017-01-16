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

//	public List<Integer> getYears() throws Exception {
//		final Connection connection = DriverManager.getConnection(
//				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
//		final List<Integer> years = new ArrayList<>();
//		ResultSet r = connection.createStatement().executeQuery(
//				"SELECT DISTINCT year FROM test ORDER BY year");
//		while (r.next())
//			years.add(r.getInt(1));
//		return years;
//	}

	public List<String> getMostSimilarWordsInYear(String givenWord,
			Integer year, int limit) throws Exception {

		String sql = "SELECT word FROM :table WHERE word1=:givenWord AND year=:year ORDER BY similarity DESC LIMIT :limit";

		try (Connection con = sql2o.open()) {
			List<String> mostSimilar = con.createQuery(sql)
					.addParameter("table", "test")
					.addParameter("givenWord", givenWord)
					.addParameter("year", year).addParameter("limit", limit)
					.executeScalarList(String.class);
			return mostSimilar;
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
