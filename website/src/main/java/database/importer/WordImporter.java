package database.importer;

import org.sql2o.Query;
import org.sql2o.Sql2o;

public class WordImporter extends Importer {

	private static final String SQL_TEMPLATE = "INSERT INTO %s (corpus, word, id) VALUES (:corpus, :word, :id)";

	public WordImporter(final Sql2o sql2o, final int corpusId,
			final String tableName) {
		super(SQL_TEMPLATE, sql2o, corpusId, tableName);
	}

	@Override
	protected Query addParameters(final String[] s, final Query query) {
		query.addParameter("word", s[0]);
		query.addParameter("id", Integer.valueOf(s[1]));
		return query;
	}

}
