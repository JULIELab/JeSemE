package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.YearAndValue;

/**
 * Compromise between Thymeleaf JSON serialization and convenience
 *
 * @author hellrich
 *
 */

public class JSON {

	final Map<String, Object> data = new HashMap<>();
	final HashMap<String, String> xs = new HashMap<>();
	final ArrayList<Object> columns = new ArrayList<>();

	public JSON() {
		data.put("xs", xs);
		data.put("columns", columns);
	}

	public JSON addValues(final String word,
			final List<YearAndValue> yearAndValueList) {
		xs.put(word, word + "-x-value");

		final List<Object> yearList = new ArrayList<>(
				yearAndValueList.size() + 1);
		yearList.add(word + "-x-value");

		final List<Object> valueList = new ArrayList<>(
				yearAndValueList.size() + 1);
		valueList.add(word);

		for (final YearAndValue yas : yearAndValueList) {
			valueList.add(yas.value);
			yearList.add(yas.year);
		}

		columns.add(valueList);
		columns.add(yearList);
		return this;
	}

}
