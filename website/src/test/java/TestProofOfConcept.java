

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Sql2o;

import com.google.common.collect.Lists;

import database.DatabaseService;
import database.YearAndValue;

public class TestProofOfConcept {

	private static DatabaseService db;

	@BeforeClass
	public static void initializeDatabase() throws Exception {
		db = new DatabaseService(new Sql2o(
				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", ""));
	}

	@Test
	public void testAssociationJSON() throws Exception {
		JSON expected = new JSON();
		expected.addValues("bar", Lists.newArrayList(new YearAndValue(1910, 0.4f),new YearAndValue(1920, 0.5f),new YearAndValue(1930, 0.2f),new YearAndValue(1940, 0.1f)));
		Map<String, Object> actual = ProofOfConcept.getAssociationJSON(db, DatabaseService.TEST_SIMILARITY, false, "foo","bar");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0), ((List<?>) actual.get("columns")).get(0));
	}
	
	@Test
	public void testGetFrequencyJSON() throws Exception {
		JSON expected = new JSON();
		expected.addValues("foo", Lists.newArrayList(new YearAndValue(1910, 0.6f),new YearAndValue(1920, 0.1f),new YearAndValue(1930, 0.3f),new YearAndValue(1940, 0.5f)));
		Map<String, Object> actual = ProofOfConcept.getFrequencyJSON(db, DatabaseService.TEST_FREQUENCY, "foo");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0), ((List<?>) actual.get("columns")).get(0));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetMostSimilarAtBeginningAndEnd() throws Exception {
		assertEquals(new String[]{"arr","bar"}, ProofOfConcept.getMostSimilarAtBeginningAndEnd(db, "foo"));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetTopContextAtBeginningAndEnd() throws Exception {
		assertEquals(new String[]{"bar","boo"}, ProofOfConcept.getTopContextAtBeginningAndEnd(db, "foo"));
	}
}