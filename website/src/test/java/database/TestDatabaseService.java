package database;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.dbunit.DatabaseUnitException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Sql2o;

public class TestDatabaseService {

	private static DatabaseService db;

	@BeforeClass
	public static void initializeDatabase() throws Exception {
		db = new DatabaseService(new Sql2o(
				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", ""));
	}

	@Test
	public void testGetYears() throws Exception {
		assertEquals(Arrays.asList(new Integer[] { 1910, 1920, 1930, 1940 }),
				db.getYears("foo"));
	}

	@Test
	public void testGetMostSimilar() throws DatabaseUnitException, Exception {
		assertEquals(Arrays.asList(new String[] { "arr" }),
				db.getMostSimilarWordsInYear("foo", 1910, 1));

		assertEquals(Arrays.asList(new String[] { "arr", "bar" }),
				db.getMostSimilarWordsInYear("foo", 1910, 2));
	}

	@Test
	public void testGetYearAndAssociation() throws Exception {
		assertEquals(Arrays.asList(new YearAndAssociation[] {
				new YearAndAssociation(1910, 0.4f),
				new YearAndAssociation(1920, 0.5f),
				new YearAndAssociation(1930, 0.2f),
				new YearAndAssociation(1940, 0.1f) }),
				db.getYearAndAssociation(DatabaseService.TEST_SIMILARITY,
						false, "foo", "bar"));
		assertEquals(Arrays.asList(new YearAndAssociation[] {
				new YearAndAssociation(1910, 0.4f),
				new YearAndAssociation(1920, 0.5f),
				new YearAndAssociation(1930, 0.2f),
				new YearAndAssociation(1940, 0.1f) }),
				db.getYearAndAssociation(DatabaseService.TEST_SIMILARITY,
						false, "bar", "foo"));
		assertEquals(Arrays.asList(new YearAndAssociation[] {
				new YearAndAssociation(1910, 23f),
				new YearAndAssociation(1930, 29f) }), db.getYearAndAssociation(
				DatabaseService.TEST_PPMI, true, "foo", "bar"));

		assertEquals(Arrays.asList(new YearAndAssociation[] {
				new YearAndAssociation(1920, 11f),
				new YearAndAssociation(1930, 31f),
				new YearAndAssociation(1940, 3f) }), db.getYearAndAssociation(
				DatabaseService.TEST_PPMI, true, "bar", "foo"));
	}

	@Test
	public void testGetTopContextWordsInYear() throws Exception {
		assertEquals(Arrays.asList(new String[] { "bar" }),
				db.getTopContextWordsInYear(DatabaseService.TEST_PPMI, "foo",
						1910, 1));
		assertEquals(Arrays.asList(new String[] { "bar", "boo" }),
				db.getTopContextWordsInYear(DatabaseService.TEST_PPMI, "foo",
						1910, 2));
	}
}
