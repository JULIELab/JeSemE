package de.julielab.jeseme.database.corpus;

public class DummyMapper implements WordMapper {

	@Override
	public String map(String s) {
		return s;
	}

}
