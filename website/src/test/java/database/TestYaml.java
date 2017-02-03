package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import static org.junit.Assert.*;

public class TestYaml {
	
	@Test
	public void test() throws FileNotFoundException{
		System.out.println(String.format(DatabaseService.WORDIDS_TEMPLATE, "foo"));

	}
}
