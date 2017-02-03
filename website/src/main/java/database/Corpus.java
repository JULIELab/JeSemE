package database;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class Corpus {
	static final String SCHEMA = "JEDISEM";
	static final String CORPORA = SCHEMA + ".TABLES";
	private static final String SIMILARITY_TEMPLATE = SCHEMA + ".%s_SIMILARITY";
	private static final String WORDIDS_TEMPLATE = SCHEMA + ".%s_WORDIDS";
	private static final String PPMI_TEMPLATE = SCHEMA + ".%s_PPMI";
	private static final String FREQUENCY_TEMPLATE = SCHEMA + ".%s_FREQUENCY";

	private final BiMap<String, Integer> word2id = HashBiMap.create();
	private final String wordIdTable;
	private final String frequencyTable;
	private final String ppmiTable;
	private final String similarityTable;

	public Corpus(String corpus) {
		wordIdTable = String.format(WORDIDS_TEMPLATE, corpus);
		frequencyTable = String.format(FREQUENCY_TEMPLATE, corpus);
		ppmiTable = String.format(PPMI_TEMPLATE, corpus);
		similarityTable = String.format(SIMILARITY_TEMPLATE, corpus);
	}

	public void addIdMapping(List<WordAndID> wordAndIds) {
		for (WordAndID wordAndId : wordAndIds)
			word2id.put(wordAndId.word, wordAndId.id);
	}

	public boolean hasMappingFor(String... words) {
		return Arrays.stream(words).allMatch(word -> word2id.containsKey(word));
	}
	
	public String getMappingFor(String word) {
		return word2id.containsKey(word);
	}

	/**
	 * @return the wordIdTable
	 */
	public String getWordIdTable() {
		return this.wordIdTable;
	}

	/**
	 * @return the frequencyTable
	 */
	public String getFrequencyTable() {
		return this.frequencyTable;
	}

	/**
	 * @return the ppmiTable
	 */
	public String getPpmiTable() {
		return this.ppmiTable;
	}

	/**
	 * @return the similarityTable
	 */
	public String getSimilarityTable() {
		return this.similarityTable;
	}
}
