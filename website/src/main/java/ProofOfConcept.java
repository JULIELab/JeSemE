import com.google.gson.Gson;

import database.DatabaseService;

import org.docopt.Docopt;
import org.sql2o.Sql2o;

import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.*;
import static spark.Spark.*;

public class ProofOfConcept {

	private static final int LIMIT = 2;
	private static final String doc =
		    "Naval Fate.\n"
		    + "\n"
		    + "Usage:\n"
		    + "  naval_fate ship new <name>...\n"
		    + "  naval_fate ship <name> move <x> <y> [--speed=<kn>]\n"
		    + "  naval_fate ship shoot <x> <y>\n"
		    + "  naval_fate mine (set|remove) <x> <y> [--moored | --drifting]\n"
		    + "  naval_fate (-h | --help)\n"
		    + "  naval_fate --version\n"
		    + "\n"
		    + "Options:\n"
		    + "  -h --help     Show this screen.\n"
		    + "  --version     Show version.\n"
		    + "  --speed=<kn>  Speed in knots [default: 10].\n"
		    + "  --moored      Moored (anchored) mine.\n"
		    + "  --drifting    Drifting mine.\n"
		    + "\n";

	public static void main(String[] args) throws Exception {
		Map<String, Object> opts =
		        new Docopt(doc).withVersion("Naval Fate 2.0").parse(args);
		      System.out.println(opts);
		   
		// System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

		Sql2o sql2o = new Sql2o("jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true",
				"SA", "");
		DatabaseService db = new DatabaseService(sql2o, "/Users/hellrich/Desktop/demo1000"); //save when multiple connections are made? move into query methods?

		db.getTables();

		staticFileLocation("/public");
		redirect.get("/", "/index.html");

		get("/search", (request, response) -> {
			String corpus = request.queryParams("corpus");
			String word = request.queryParams("word");
			long t = System.currentTimeMillis();
			String[] mostSimilar = getMostSimilarAtBeginningAndEnd(db, corpus, word);
			System.out.println("most "+(System.currentTimeMillis()-t));t = System.currentTimeMillis();
			Map<String, Object> model = new HashMap<>();
			model.put("word", word);
			model.put("corpus", corpus);
			model.put("similaritydata", getAssociationJSON(db, corpus,
					DatabaseService.SIMILARITY_TABLE, false, word, mostSimilar));
			System.out.println("sim "+(System.currentTimeMillis()-t));t = System.currentTimeMillis();
			model.put("ppmidata",
					getAssociationJSON(db, corpus, DatabaseService.PPMI_TABLE, true,
							word, getTopContextAtBeginningAndEnd(db, corpus, word)));
			System.out.println("ppmi "+(System.currentTimeMillis()-t));t = System.currentTimeMillis();
			model.put("frequencydata",
					getFrequencyJSON(db, corpus, word));
			System.out.println("freq "+(System.currentTimeMillis()-t));t = System.currentTimeMillis();
			return new ModelAndView(model, "result");
		}, new ThymeleafTemplateEngine());

		get("/api/similarity", (request, response) -> {
			String corpus = request.queryParams("corpus");
			String word1 = request.queryParams("word1");
			String word2 = request.queryParams("word2");
			return getAssociationJSON(db, corpus, DatabaseService.SIMILARITY_TABLE,
					false, word1, word2);
		}, new Gson()::toJson);

		get("/api/ppmi", (request, response) -> {
			String corpus = request.queryParams("corpus");
			String word1 = request.queryParams("word1");
			String word2 = request.queryParams("word2");
			return getAssociationJSON(db, corpus, DatabaseService.PPMI_TABLE, true,
					word1, word2);
		}, new Gson()::toJson);

		get("/api/frequency", (request, response) -> {
			String corpus = request.queryParams("corpus");
			String word = request.queryParams("word");
			return getFrequencyJSON(db, corpus, word);
		}, new Gson()::toJson);
	}

	static final String[] getMostSimilarAtBeginningAndEnd(DatabaseService db,String corpus,
			String word) throws Exception {
		List<Integer> years = db.getYears(corpus,word);
		Set<String> mostSimilar = new HashSet<>();
		mostSimilar.addAll(
				db.getMostSimilarWordsInYear(corpus, word, years.get(0), LIMIT));
		mostSimilar.addAll(db.getMostSimilarWordsInYear(corpus, word,
				years.get(years.size() - 1), LIMIT));
		return mostSimilar.toArray(new String[mostSimilar.size()]);
	}

	static final String[] getTopContextAtBeginningAndEnd(DatabaseService db, String corpus,
			String word) throws Exception {
		List<Integer> years = db.getYears(corpus, word);
		Set<String> topContext = new HashSet<>();
		topContext.addAll(db.getTopContextWordsInYear(corpus,
				word, years.get(0), LIMIT));
		topContext.addAll(db.getTopContextWordsInYear(corpus,
				word, years.get(years.size() - 1), LIMIT));
		return topContext.toArray(new String[topContext.size()]);
	}

	//TODO: functional interface to merge both?
	static final Map<String, Object> getAssociationJSON(DatabaseService db, String corpus,
			String table, boolean isContextQuery, String initialWord,
			String... moreWords) throws Exception {
		JSON data = new JSON();
		for (String word : moreWords) {
			data.addValues(word, db.getYearAndAssociation(corpus, table, isContextQuery,
					initialWord, word));
		}
		return data.data;
	}

	static final Map<String, Object> getFrequencyJSON(DatabaseService db, String corpus,
			String word) throws Exception {
		return new JSON().addValues(word,
				db.getYearAndFrequency(corpus, word)).data;
	}

}
