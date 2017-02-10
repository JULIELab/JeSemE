
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import database.DatabaseService;
import database.TestDatabaseService;
import database.YearAndValue;

public class TestProofOfConcept {

	private static DatabaseService db;
	private static final String CORPUS = "test1";

	@BeforeClass
	public static void before() throws Exception {
		db = TestDatabaseService.initializeDatabase();
	}

	@Test
	public void testAssociationJSON() throws Exception {
		JSON expected = new JSON();
		expected.addValues("bar", Lists.newArrayList(
				new YearAndValue(1910, 0.4f), new YearAndValue(1920, 0.5f),
				new YearAndValue(1930, 0.2f), new YearAndValue(1940, 0.1f)));
		Map<String, Object> actual = ProofOfConcept.getAssociationJSON(db,
				CORPUS, DatabaseService.SIMILARITY_TABLE, false, "foo", "bar");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0),
				((List<?>) actual.get("columns")).get(0));
	}

	@Test
	public void testGetFrequencyJSON() throws Exception {
		JSON expected = new JSON();
		expected.addValues("foo", Lists.newArrayList(
				new YearAndValue(1910, 0.6f), new YearAndValue(1920, 0.1f),
				new YearAndValue(1930, 0.3f), new YearAndValue(1940, 0.5f)));
		Map<String, Object> actual = ProofOfConcept.getFrequencyJSON(db, CORPUS,
				"foo");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0),
				((List<?>) actual.get("columns")).get(0));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetMostSimilarAtBeginningAndEnd() throws Exception {
		assertEquals(new String[] { "arr", "bar" }, ProofOfConcept
				.getMostSimilarAtBeginningAndEnd(db, CORPUS, "foo"));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetTopContextAtBeginningAndEnd() throws Exception {
		assertEquals(new String[] { "bar", "boo" }, ProofOfConcept
				.getTopContextAtBeginningAndEnd(db, CORPUS, "foo"));
	}
}