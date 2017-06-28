package de.julielab.jeseme.embeddings;

import java.util.Arrays;

public class Embedding {
	final double[] embedding;

	public Embedding(double... embedding){
		this.embedding = embedding;
	}
	
	public Embedding(String lineToParse){
		String[] s = lineToParse.split(" ");
		embedding = new double[s.length];
		for(int i = 0; i<embedding.length; ++i)
			embedding[i] = Double.parseDouble(s[i]);
	}
	
	/**
	 * Assumes normalized embedding vectors
	 */
	public double similarity(Embedding other){
		if(this.embedding.length != other.embedding.length)
			throw new IllegalArgumentException("Dimensions not matching");
		double similarity = 0;
		for(int i = 0; i<this.embedding.length; ++i)
			similarity += this.embedding[i] * other.embedding[i];
		return similarity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Embedding [embedding=" + Arrays.toString(this.embedding) + "]";
	}
}
