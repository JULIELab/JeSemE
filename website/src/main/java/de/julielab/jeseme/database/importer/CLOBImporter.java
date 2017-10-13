package de.julielab.jeseme.database.importer;

import java.util.Arrays;

import org.sql2o.Query;
import org.sql2o.Sql2o;

import com.google.common.base.Joiner;

public class CLOBImporter extends Importer {

	private static final String SQL_TEMPLATE = "INSERT INTO %s (corpus, word, year, clob) VALUES (:corpus, :word, :year, :clob)";

	//CLOB = Character Large Object, i.e., generic palyoad as text, e.g., embeddings
	public CLOBImporter(final Sql2o sql2o, final int corpusId,
			final String tableName) {
		super(SQL_TEMPLATE, sql2o, corpusId, tableName);
	}

	@Override
	protected Query addParameters(final String[] s, final Query query) {
		query.addParameter("word", Integer.valueOf(s[0]));
		query.addParameter("year", Integer.valueOf(s[1]));
		query.addParameter("clob",
				Joiner.on(" ").join(Arrays.copyOfRange(s, 2, s.length)));
		return query;
	}

}