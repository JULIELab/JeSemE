package de.julielab.jeseme.database.importer;

import org.sql2o.Query;
import org.sql2o.Sql2o;

public class EmbeddingImporter extends Importer {

	private static final String SQL_TEMPLATE = "INSERT INTO %s (corpus, word, year, embedding) VALUES (:corpus, :word, :year, :embedding)";

	public EmbeddingImporter(final Sql2o sql2o, final int corpusId,
			final String tableName) {
		super(SQL_TEMPLATE, sql2o, corpusId, tableName);
	}

	@Override
	protected Query addParameters(final String[] s, final Query query) {
		query.addParameter("word", Integer.valueOf(s[0]));
		query.addParameter("year", Integer.valueOf(s[1]));
		query.addParameter("embedding", s[2]);
		return query;
	}

}