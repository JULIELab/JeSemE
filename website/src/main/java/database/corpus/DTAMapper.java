package database.corpus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DTAMapper implements WordMapper {

	private final Map<String, String> mapping = new HashMap<>();

	public DTAMapper(Path path) throws IOException {
		Files.lines(path).map(line -> line.split(";"))
				.forEach(s -> mapping.put(s[0], s[1]));
	}

	@Override
	public String map(String s) {
		String m = mapping.get(s);
		if (m == null)
			return s;
		return m;
	}

}
