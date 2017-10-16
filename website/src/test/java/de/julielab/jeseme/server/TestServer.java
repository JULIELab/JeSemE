package de.julielab.jeseme.server;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.julielab.jeseme.database.DatabaseService;
import de.julielab.jeseme.database.YearAndValue;
import de.julielab.jeseme.helper.DatabaseServiceHelper;

public class TestServer {

	private static DatabaseServiceHelper helper;
	private static DatabaseService db;
	private static final String CORPUS = "test1";

	@AfterClass
	public static void after() {
		helper.after();
	}

	@BeforeClass
	public static void before() throws Exception {
		helper = new DatabaseServiceHelper();
		db = helper.getDatabaseService();
	}

	@Test
	public void testAssociationJSON() throws Exception {
		final JSON expected = new JSON();
		expected.addValues("bar", Lists.newArrayList(
				new YearAndValue(1920, 23f), new YearAndValue(1930, 29f)));
		final Map<String, Object> actual = Server.getAssociationJson(db, CORPUS,
				DatabaseService.PPMI_TABLE, "foo", "bar");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0),
				((List<?>) actual.get("columns")).get(0));
	}

	@Test
	public void testEmotionJSON() throws Exception {
		final JSON expected = new JSON();
		expected.addValues("valence", Lists.newArrayList(
				new YearAndValue(1910, 0.4f), new YearAndValue(1930, 0.7f)));
		expected.addValues("arousal", Lists.newArrayList(
				new YearAndValue(1910, 0.5f), new YearAndValue(1930, -3f)));
		expected.addValues("dominance", Lists.newArrayList(
				new YearAndValue(1910, 6f), new YearAndValue(1930, 5.55f)));
		final Map<String, Object> actual = Server.getEmotionJson(db, CORPUS,
				"bar");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0),
				((List<?>) actual.get("columns")).get(0));
	}

	@Test
	public void testGetFrequencyJSON() throws Exception {
		final JSON expected = new JSON();
		expected.addValues("foo", Lists.newArrayList(
				new YearAndValue(1910, 0.6f), new YearAndValue(1920, 0.1f),
				new YearAndValue(1930, 0.3f), new YearAndValue(1940, 0.5f)));
		final Map<String, Object> actual = Server.getFrequencyJson(db, CORPUS,
				"foo");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0),
				((List<?>) actual.get("columns")).get(0));
	}

	@Test
	public void testGetTopContextAtBeginningAndEnd() throws Exception {
		assertArrayEquals(new String[] { "bar", "boo" },
				Server.getTopContextAtBeginningAndEnd(db,
						DatabaseService.PPMI_TABLE, CORPUS, "foo"));
	}

	@Test
	public void testSimilarityJSON() throws Exception {
		final JSON expected = new JSON();
		expected.addValues("bar", Lists.newArrayList(new YearAndValue(1910, 0f),
				new YearAndValue(1920, 0f), new YearAndValue(1930, 1f)));
		final Map<String, Object> actual = Server.getSimilarityJson(db, CORPUS,
				"foo", "bar");
		assertEquals(expected.data.get("xs"), actual.get("xs"));
		assertEquals(((List<?>) expected.data.get("columns")).get(0),
				((List<?>) actual.get("columns")).get(0));
	}
}