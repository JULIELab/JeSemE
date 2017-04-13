package de.julielab.jeseme.database.importer;

public interface AcceptanceCriterium {
	
	default boolean accepts(String[] s){
		return true;
	}
}
