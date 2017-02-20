package configuration;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Test;

public class TestConfiguration {
	@Test
	public void test() throws FileNotFoundException{
		Configuration config = Configuration.readYamlFile("src/test/resources/config.yaml");
		assertTrue(config.coversServer());
		assertEquals("not really an ip", config.getServer().getIp());
	}
}
