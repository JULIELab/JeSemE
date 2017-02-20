package configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;

public class TestConfiguration {
	@Test
	public void test() throws FileNotFoundException {
		final Configuration config = Configuration
				.readYamlFile("src/test/resources/config.yaml");
		assertTrue(config.coversServer());
		assertEquals("127.0.0.1", config.getServer().getIp());
	}
}
