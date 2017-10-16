package de.julielab.jeseme.database;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.julielab.jeseme.database.DatabaseService;
import de.julielab.jeseme.database.YearAndValue;
import de.julielab.jeseme.embeddings.Embedding;
import de.julielab.jeseme.helper.DatabaseServiceHelper;

public class TestDatabaseService {

	private static final String CORPUS = "test1";
	private static DatabaseServiceHelper helper;
	private static DatabaseService db;

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
	public void testGetSimilarity() throws DatabaseUnitException, Exception {
		assertEquals(
				Lists.newArrayList(new YearAndValue(1910, 2f),
						new YearAndValue(1920, 1f), new YearAndValue(1930, 3f),
						new YearAndValue(1940, 1f)),
				db.getSimilarity(CORPUS, "foo", "foo"));
	}

	@Test
	public void testGetEmbedding() throws DatabaseUnitException, Exception {
		Map<Integer, Embedding> year2Embedding = db.getEmbedding(CORPUS, "foo");
		assertEquals(Sets.newHashSet(1910, 1920, 1930, 1940),
				year2Embedding.keySet());
		assertEquals(1,
				year2Embedding.get(1910).similarity(year2Embedding.get(1920)),
				0.00001);
	}
	
	@Test
	public void testGetEmotion() throws DatabaseUnitException, Exception {
		Map<String, List<YearAndValue>> year2Embedding = db.getEmotion(CORPUS, "bar");
		assertEquals(Lists.newArrayList(new YearAndValue(1910, 0.4f),
			new YearAndValue(1930, 0.7f)), year2Embedding.get("valence"));
		assertEquals(Lists.newArrayList(new YearAndValue(1910, 0.5f),
				new YearAndValue(1930, -3f)), year2Embedding.get("arousal"));
		assertEquals(Lists.newArrayList(new YearAndValue(1910, 6f),
				new YearAndValue(1930, 5.55f)), year2Embedding.get("dominance"));
	}

	@Test
	public void testGetMostSimilar() throws DatabaseUnitException, Exception {
		assertEquals(Arrays.asList(new String[] { "arr" }),
				db.getTopContextWordsInYear(CORPUS, DatabaseService.SIMILARITY_TABLE, "foo", 1910, 1));

		assertEquals(Arrays.asList(new String[] { "arr", "boo" }),
				db.getTopContextWordsInYear(CORPUS, DatabaseService.SIMILARITY_TABLE, "foo", 1910, 2));
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
						DatabaseService.SIMILARITY_TABLE, "bar", "foo"));
		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1910, 23f),
						new YearAndValue(1930, 29f) }),
				db.getYearAndAssociation(CORPUS, DatabaseService.PPMI_TABLE,
						"foo", "bar"));

		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1920, 11f),
						new YearAndValue(1930, 31f),
						new YearAndValue(1940, 3f) }),
				db.getYearAndAssociation(CORPUS, DatabaseService.PPMI_TABLE,
						"bar", "foo"));
		assertEquals(
				Arrays.asList(
						new YearAndValue[] { new YearAndValue(1950, 23f), }),
				db.getYearAndAssociation(CORPUS, DatabaseService.CHI_TABLE,
						"foo", "bar"));

		assertEquals(
				Arrays.asList(new YearAndValue[] { new YearAndValue(1960, 11f),
						new YearAndValue(1970, 31f),
						new YearAndValue(1980, 3f) }),
				db.getYearAndAssociation(CORPUS, DatabaseService.CHI_TABLE,
						"bar", "foo"));
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
		assertEquals(new Integer(1910), db.getFirstYear(CORPUS, "foo"));
		assertEquals(new Integer(1940), db.getLastYear(CORPUS, "foo"));
	}

	@Test
	public void testGetYearsWithMapping() throws Exception {
		assertEquals(new Integer(1910), db.getFirstYear(CORPUS, "fooo"));
		assertEquals(new Integer(1940), db.getLastYear(CORPUS, "fooo"));
	}

}
