package de.julielab.jeseme.database.corpus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DTAMapper implements WordMapper {

	private final Map<String, String> mapping = new HashMap<>();
	private final boolean lowercase;

	public DTAMapper(final Path path, boolean lowercase) throws IOException {
		final Iterator<String[]> iter = Files.lines(path)
				.map(line -> line.split(";")).iterator();
		while (iter.hasNext()) {
			final String[] s = iter.next();
			try {
				mapping.put(s[0], s[1]);
			} catch (final ArrayIndexOutOfBoundsException e) {
				System.err.println(Arrays.toString(s));
			}
		}
		this.lowercase = lowercase;
	}

	@Override
	public String map(String s) {
		s = mapping.containsKey(s) ? mapping.get(s) : s ;
		if(lowercase)
			s = s.toLowerCase();
		return s;
	}

}
