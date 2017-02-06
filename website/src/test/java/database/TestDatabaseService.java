package database;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

import org.dbunit.DatabaseUnitException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Sql2o;

public class TestDatabaseService {

	private static final String TABLE = "test1";
	private static DatabaseService db;

	@BeforeClass
	public static void initializeDatabase() throws Exception {
		db = new DatabaseService(
				new Sql2o("jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA",
						""));
		assertEquals(new HashSet<String>(Arrays.asList("test1", "test2")),db.corpora.keySet());
	}

	@Test
	public void testGetYears() throws Exception {
		assertEquals(Arrays.asList(new Integer[] { 1910, 1920, 1930, 1940 }),
				db.getYears(TABLE, "foo"));
	}

	@Test
	public void testGetMostSimilar() throws DatabaseUnitException, Exception {
		assertEquals(Arrays.asList(new String[] { "arr" }),
				db.getMostSimilarWordsInYear(TABLE, "foo", 1910, 1));

		assertEquals(Arrays.asList(new String[] { "arr", "bar" }),
				db.getMostSimilarWordsInYear(TABLE, "foo", 1910, 2));
	}

	@Test
	public void testGetYearAndAssociation() throws Exception {
		assertEquals(Arrays.asList(new YearAndValue[] {
				new YearAndValue(1910, 0.4f), new YearAndValue(1920, 0.5f),
				new YearAndValue(1930, 0.2f), new YearAndValue(1940, 0.1f) }),
				db.getYearAndAssociation(TABLE,
						DatabaseService.SIMILARITY_TABLE, false, "foo", "bar"));
		assertEquals(Arrays.asList(new YearAndValue[] {
				new YearAndValue(1910, 0.4f), new YearAndValue(1920, 0.5f),
				new YearAndValue(1930, 0.2f), new YearAndValue(1940, 0.1f) }),
				db.getYearAndAssociation(TABLE,
						DatabaseService.SIMILARITY_TABLE, false, "bar", "foo"));
		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1910, 23f),
						new YearAndValue(1930, 29f) }),
				db.getYearAndAssociation(TABLE, DatabaseService.PPMI_TABLE,
						true, "foo", "bar"));

		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1920, 11f),
						new YearAndValue(1930, 31f),
						new YearAndValue(1940, 3f) }),
				db.getYearAndAssociation(TABLE, DatabaseService.PPMI_TABLE,
						true, "bar", "foo"));
	}

	@Test
	public void testGetTopContextWordsInYear() throws Exception {
		assertEquals(Arrays.asList(new String[] { "bar" }),
				db.getTopContextWordsInYear(TABLE, "foo", 1910, 1));
		assertEquals(Arrays.asList(new String[] { "bar", "boo" }),
				db.getTopContextWordsInYear(TABLE, "foo", 1910, 2));
	}

	@Test
	public void testGetYearAndFrequency() throws Exception {
		assertEquals(Arrays.asList(new YearAndValue[] {
				new YearAndValue(1910, 0.6f), new YearAndValue(1920, 0.1f),
				new YearAndValue(1930, 0.3f), new YearAndValue(1940, 0.5f) }),
				db.getYearAndFrequency(TABLE, "foo"));
	}

}
