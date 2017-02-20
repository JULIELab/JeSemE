package database.corpus;

public class LowerCaseMapper implements WordMapper {

	@Override
	public String map(final String s) {
		return s.toLowerCase();
	}

}
