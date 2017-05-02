package de.julielab.jeseme.embeddings;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class TestEmbedding {

	@Test
	public void testSimilarity() {
		assertEquals(0, new Embedding(0).similarity(new Embedding(1)), 0.0001);
		assertEquals(1, new Embedding(1).similarity(new Embedding(1)), 0.0001);
		assertEquals(0, new Embedding(0, 1).similarity(new Embedding(1, 0)), 0.0001);
		assertEquals(1, new Embedding(1, 1).similarity(new Embedding(1, 0)), 0.0001);
		assertEquals(0.5, new Embedding(0, 1).similarity(new Embedding(0.5, 0.5)), 0.0001);
		assertEquals(0.25, new Embedding(0, 0.5, 0.5).similarity(new Embedding(0.5, 0.25, 0.25)), 0.0001);
	}

	@Test
	public void testParsingConstructor() {
		assertTrue(Arrays.equals(new Embedding("0").embedding, new Embedding(0).embedding));
		assertTrue(Arrays.equals(new Embedding("0 -3.4 11").embedding, new Embedding(0, -3.4, 11).embedding));
		assertTrue(Arrays.equals(new Embedding("0 34 11\n").embedding, new Embedding(0, 34, 11).embedding));
	}

}
