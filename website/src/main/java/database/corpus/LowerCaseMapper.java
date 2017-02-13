package database.corpus;

public class LowerCaseMapper implements WordMapper{

	@Override
	public String map(String s) {
		return s.toLowerCase();
	}

}
