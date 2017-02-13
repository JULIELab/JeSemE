package database.importer;

import org.sql2o.Query;
import org.sql2o.Sql2o;

public class FrequencyImporter extends Importer {

	private static final String SQL_TEMPLATE = "INSERT INTO %s (corpus, word, year, frequency) VALUES (:corpus, :word, :year, :frequency)";

	public FrequencyImporter(final Sql2o sql2o, final int corpusId,
			final String tableName) {
		super(SQL_TEMPLATE, sql2o, corpusId, tableName);
	}

	@Override
	protected Query addParameters(final String[] s, final Query query) {
		query.addParameter("word", Integer.valueOf(s[0]));
		query.addParameter("year", Integer.valueOf(s[1]));
		query.addParameter("frequency", Float.valueOf(s[2]));
		return query;
	}

}