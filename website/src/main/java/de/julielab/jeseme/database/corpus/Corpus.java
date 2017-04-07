package de.julielab.jeseme.database.corpus;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.julielab.jeseme.database.WordAndID;

public class Corpus {

	private final BiMap<String, Integer> word2id = HashBiMap.create();
	private final int id;
	private final WordMapper mapper;
	private final String fullName;
	private final String note;
	private final String url;
	private final boolean insertInUrl;
	

	public Corpus(final int id, final List<WordAndID> wordAndIds,
			final WordMapper mapper, final String fullName, final String note, final String url,
	final boolean insertInUrl) {
		this.id = id;
		for (final WordAndID wordAndId : wordAndIds)
			word2id.put(wordAndId.word, wordAndId.id);
		this.mapper = mapper;
		this.fullName = fullName;
		this.note = note;
		this.url = url;
		this.insertInUrl = insertInUrl;
	}

	public int getId() {
		return id;
	}

	public String getStringFor(final Integer id) {
		return word2id.inverse().get(id);
	}

	public Integer getIdFor(final String word) {
		return word2id.get(mapper.map(word));
	}

	public boolean hasMappingFor(final String... words) {
		return Arrays.stream(words).map(w -> mapper.map(w))
				.allMatch(word -> word2id.containsKey(word));
	}

	public String getFullName() {
		return fullName;
	}

	public String getNote() {
		return note;
	}
	
	public String getUrl(String word) {
		if(!insertInUrl)
			return url;
		return String.format(url, word);
	}
}
