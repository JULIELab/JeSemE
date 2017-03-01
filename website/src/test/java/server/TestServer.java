package server;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import configuration.Configuration;
import database.DatabaseService;
import database.TestDatabaseService;
import database.YearAndValue;

public class TestServer {

	private static DatabaseService db;
	private static final String CORPUS = "test1";

	@AfterClass
	public static void after() {
		db.dropAll();
	}

	@BeforeClass
	public static void before() throws Exception {
		db = TestDatabaseService.initializeDatabase();
	}

	@Test
	public void testAssociationJSON() throws Exception {
		final JSON expected = new JSON();
		expected.addValues("bar", Lists.newArrayList(
				new YearAndValue(1910, 0.4f), new YearAndValue(1920, 0.5f),
				new YearAndValue(1930, 0.2f), new YearAndValue(1940, 0.1f)));
		final Map<String, Object> actual = Server.getAssociationJson(db, CORPUS,
				DatabaseService.SIMILARITY_TABLE, false, "foo", "bar");
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
	public void testGetMostSimilarAtBeginningAndEnd() throws Exception {
		assertArrayEquals(new String[] { "arr", "bar" },
				Server.getMostSimilarAtBeginningAndEnd(db, CORPUS, "foo"));
	}

	@Test
	public void testGetTopContextAtBeginningAndEnd() throws Exception {
		assertArrayEquals(new String[] { "bar", "boo" },
				Server.getTopContextAtBeginningAndEnd(db,
						DatabaseService.PPMI_TABLE, CORPUS, "foo"));
	}

	@Test
	public void testStartPingStop() throws Exception {
		Server.startServer(db,
				Configuration.readYamlFile("src/test/resources/config.yaml"));
		try (Socket s = new Socket("127.0.0.1", 6666)) {
			//nothing to do
		} catch (final Exception e) {
			org.junit.Assert.fail("Server not working");
			throw (e);
		}
		Server.stopServer();
	}
}