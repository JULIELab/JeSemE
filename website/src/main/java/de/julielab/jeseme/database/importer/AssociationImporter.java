package de.julielab.jeseme.database.importer;

import org.sql2o.Query;
import org.sql2o.Sql2o;

public class AssociationImporter extends Importer {
	private static final String SQL_TEMPLATE = "INSERT INTO %s (corpus, word1, word2, year, association) VALUES (:corpus, :word1, :word2, :year, :association)";

	public AssociationImporter(final Sql2o sql2o, final int corpusId,
			final String tableName) {
		super(SQL_TEMPLATE, sql2o, corpusId, tableName);
	}

	@Override
	protected Query addParameters(final String[] s, final Query query) {
		query.addParameter("word1", Integer.valueOf(s[0]));
		query.addParameter("word2", Integer.valueOf(s[1]));
		query.addParameter("year", Integer.valueOf(s[2]));
		query.addParameter("association", Float.valueOf(s[3]));
		return query;
	}
}