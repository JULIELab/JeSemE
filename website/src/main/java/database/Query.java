package database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dbunit.DatabaseUnitException;

public class Query {

	public Query() throws DatabaseUnitException, Exception {
		readDemo();
	}

	public Object[] getSimilarity(String word1, String word2) throws Exception {
		final Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
		ResultSet r = connection.createStatement().executeQuery(
				"SELECT year, similarity FROM test WHERE word1='" + word1
						+ "' and word2='" + word2 + "' ORDER BY year ASC");
		ArrayList<Object> result = new ArrayList<>();
		result.add(word1);
		while (r.next())
			result.add(r.getFloat("similarity"));
		return result.toArray();
	}

	public List<Integer> getYears() throws Exception {
		final Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
		final List<Integer> years = new ArrayList<>();
		ResultSet r = connection.createStatement().executeQuery(
				"SELECT DISTINCT year FROM test ORDER BY year");
		while (r.next())
			years.add(r.getInt(1));
		return years;
	}

	public Object[] getMostSimilar(String word, Integer year, int limit)
			throws Exception {
		final Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
		ArrayList<Object[]> result = new ArrayList<>();
		ResultSet r2 = connection.createStatement().executeQuery(
				"SELECT word2, similarity FROM test WHERE word1='" + word
						+ "' AND year=+" + year
						+ " ORDER BY similarity DESC LIMIT " + limit);
		while (r2.next()) {
			result.add(new Object[] { r2.getString(1), r2.getFloat(2) });
		}
		return result.toArray();
	}

	static void readDemo() throws Exception{
		final Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
		Statement st = connection.createStatement();
		connection
				.createStatement()
				.execute(
						"CREATE TABLE test (word1 TEXT, word2 TEXT, year SMALLINT, similarity REAL, PRIMARY KEY(word1, word2,year) );");
		for(String[] x : Files.lines(Paths.get("src/test/resources/CrossSimilarity.demo")).map(x -> x.split(" ")).collect(Collectors.toList()))
				st.execute(
				"INSERT INTO test VALUES ('"+x[0]+"','"+x[1]+"',"+x[2]+","+x[3]+")"
						);
		
	}
}
