package de.julielab.jeseme.database.importer;

import org.sql2o.Sql2o;

public class AssociationImporterWithMinimum extends AssociationImporter {
	
	private Float minimum;

	public AssociationImporterWithMinimum(final Sql2o sql2o, final int corpusId,
			final String tableName, final float minimum) {
		super(sql2o, corpusId, tableName);
		this.minimum = minimum;
	}
	
	@Override
	public boolean accepts(String[] s) {
		return Float.valueOf(s[3]) >= minimum;
	}
	
}