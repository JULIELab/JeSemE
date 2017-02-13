package database.corpus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DTAMapper implements WordMapper {

	private final Map<String, String> mapping = new HashMap<>();

	public DTAMapper(Path path) throws IOException {
		Iterator<String[]> iter = Files.lines(path).map(line -> line.split(";"))
				.iterator();
		while (iter.hasNext()) {
			String[] s = iter.next();
			try {
				mapping.put(s[0], s[1]);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println(Arrays.toString(s));
			}
		}
	}

	@Override
	public String map(String s) {
		String m = mapping.get(s);
		if (m == null)
			return s;
		return m;
	}

}
