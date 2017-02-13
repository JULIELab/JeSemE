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

	public Corpus(int id, List<WordAndID> wordAndIds, WordMapper mapper) {
		this.id = id;
		for (WordAndID wordAndId : wordAndIds)
			word2id.put(wordAndId.word, wordAndId.id);
		this.mapper = mapper;
	}

	public int getId() {
		return id;
	}

	public boolean hasMappingFor(String... words) {
		return Arrays.stream(words).map(w -> mapper.map(w))
				.allMatch(word -> word2id.containsKey(word));
	}

	public Integer getIdFor(String word) {
		return word2id.get(mapper.map(word));
	}

	public String getIdFor(Integer id) {
		return word2id.inverse().get(id);
	}

}
