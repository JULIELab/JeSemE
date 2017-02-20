package database.corpus;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import database.WordAndID;

public class Corpus {

	private final BiMap<String, Integer> word2id = HashBiMap.create();
	private final int id;
	private final WordMapper mapper;

	public Corpus(final int id, final List<WordAndID> wordAndIds,
			final WordMapper mapper) {
		this.id = id;
		for (final WordAndID wordAndId : wordAndIds)
			word2id.put(wordAndId.word, wordAndId.id);
		this.mapper = mapper;
	}

	public int getId() {
		return id;
	}

	public String getIdFor(final Integer id) {
		return word2id.inverse().get(id);
	}

	public Integer getIdFor(final String word) {
		return word2id.get(mapper.map(word));
	}

	public boolean hasMappingFor(final String... words) {
		return Arrays.stream(words).map(w -> mapper.map(w))
				.allMatch(word -> word2id.containsKey(word));
	}

}
