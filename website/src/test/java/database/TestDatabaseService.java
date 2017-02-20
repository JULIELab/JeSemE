package database;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.dbunit.DatabaseUnitException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Sql2o;

import com.zaxxer.hikari.HikariDataSource;

import configuration.Configuration;

public class TestDatabaseService {

	private static final String CORPUS = "test1";
	private static DatabaseService db;

	@AfterClass
	public static void after() {
		db.dropAll();
	}

	@BeforeClass
	public static void before() throws Exception {
		db = initializeDatabase();
		assertEquals(new HashSet<>(Arrays.asList("test1", "test2")),
				db.corpora.keySet());
	}

	public static DatabaseService initializeDatabase() throws Exception {
		final Configuration config = Configuration
				.readYamlFile("src/test/resources/config.yaml");
		final HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(config.getDatabase().getUrl());
		ds.setUsername(config.getDatabase().getUser());
		ds.setPassword(config.getDatabase().getPassword());

		final Sql2o sql2o = new Sql2o(ds);

		DatabaseService.initializeTables(sql2o);
		DatabaseService.importTables(config, sql2o);

		return new DatabaseService(sql2o, config);
	}

	@Test
	public void testGetMostSimilar() throws DatabaseUnitException, Exception {
		assertEquals(Arrays.asList(new String[] { "arr" }),
				db.getMostSimilarWordsInYear(CORPUS, "foo", 1910, 1));

		assertEquals(Arrays.asList(new String[] { "arr", "bar" }),
				db.getMostSimilarWordsInYear(CORPUS, "foo", 1910, 2));
	}

	@Test
	public void testGetTopContextWordsInYear() throws Exception {
		assertEquals(Arrays.asList(new String[] { "bar" }),
				db.getTopContextWordsInYear(CORPUS, DatabaseService.PPMI_TABLE,
						"foo", 1910, 1));
		assertEquals(Arrays.asList(new String[] { "bar", "boo" }),
				db.getTopContextWordsInYear(CORPUS, DatabaseService.PPMI_TABLE,
						"foo", 1910, 2));
	}

	@Test
	public void testGetYearAndAssociation() throws Exception {
		assertEquals(Arrays.asList(new YearAndValue[] {
				new YearAndValue(1910, 0.4f), new YearAndValue(1920, 0.5f),
				new YearAndValue(1930, 0.2f), new YearAndValue(1940, 0.1f) }),
				db.getYearAndAssociation(CORPUS,
						DatabaseService.SIMILARITY_TABLE, false, "foo", "bar"));
		assertEquals(Arrays.asList(new YearAndValue[] {
				new YearAndValue(1910, 0.4f), new YearAndValue(1920, 0.5f),
				new YearAndValue(1930, 0.2f), new YearAndValue(1940, 0.1f) }),
				db.getYearAndAssociation(CORPUS,
						DatabaseService.SIMILARITY_TABLE, false, "bar", "foo"));
		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1910, 23f),
						new YearAndValue(1930, 29f) }),
				db.getYearAndAssociation(CORPUS, DatabaseService.PPMI_TABLE,
						true, "foo", "bar"));

		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1920, 11f),
						new YearAndValue(1930, 31f),
						new YearAndValue(1940, 3f) }),
				db.getYearAndAssociation(CORPUS, DatabaseService.PPMI_TABLE,
						true, "bar", "foo"));
		assertEquals(
				Arrays.asList(
						new YearAndValue[] { new YearAndValue(1950, 23f), }),
				db.getYearAndAssociation(CORPUS, DatabaseService.CHI_TABLE,
						true, "foo", "bar"));

		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1960, 11f),
						new YearAndValue(1970, 31f),
						new YearAndValue(1980, 3f) }),
				db.getYearAndAssociation(CORPUS, DatabaseService.CHI_TABLE,
						true, "bar", "foo"));
	}

	@Test
	public void testGetYearAndFrequency() throws Exception {
		assertEquals(Arrays.asList(new YearAndValue[] {
				new YearAndValue(1910, 0.6f), new YearAndValue(1920, 0.1f),
				new YearAndValue(1930, 0.3f), new YearAndValue(1940, 0.5f) }),
				db.getYearAndFrequency(CORPUS, "foo"));
	}

	@Test
	public void testGetYears() throws Exception {
		assertEquals(Arrays.asList(new Integer[] { 1910, 1920, 1930, 1940 }),
				db.getYears(CORPUS, "foo"));
	}

	@Test
	public void testGetYearsWithMapping() throws Exception {
		assertEquals(Arrays.asList(new Integer[] { 1910, 1920, 1930, 1940 }),
				db.getYears(CORPUS, "fooo"));
	}

}
