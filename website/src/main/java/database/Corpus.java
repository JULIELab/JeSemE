package database;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class Corpus {


	private final BiMap<String, Integer> word2id = HashBiMap.create();
	private final int id;
	
	public Corpus(int id, List<WordAndID> wordAndIds) {
		this.id = id;
		for (WordAndID wordAndId : wordAndIds)
			word2id.put(wordAndId.word, wordAndId.id);
	}

	public int getId(){
		return id;
	}
	
	public boolean hasMappingFor(String... words) {
		return Arrays.stream(words).allMatch(word -> word2id.containsKey(word));
	}
	
	public Integer getIdFor(String word) {
		return word2id.get(word);
	}
	
	public String getIdFor(Integer id){
		return word2id.inverse().get(id);
	}
}
