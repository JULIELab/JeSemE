import com.google.gson.Gson;

import database.DatabaseService;

import org.sql2o.Sql2o;

import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.*;
import static spark.Spark.*;

public class ProofOfConcept {

	private static final int LIMIT = 2;

	public static void main(String[] args) throws Exception {
		// System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

		Sql2o sql2o = new Sql2o("jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true",
				"SA", "");
		DatabaseService db = new DatabaseService(sql2o); //save when multiple connections are made? move into query methods?

		db.getTables();

		staticFileLocation("/public");
		redirect.get("/", "/index.html");

		get("/search", (request, response) -> {
			String word = request.queryParams("word");
			String[] mostSimilar = getMostSimilarAtBeginningAndEnd(db, word);

			Map<String, Object> model = new HashMap<>();
			model.put("word", word);
			model.put("similaritydata", getAssociationJSON(db,
					DatabaseService.TEST_SIMILARITY, false, word, mostSimilar));
			model.put("ppmidata",
					getAssociationJSON(db, DatabaseService.TEST_PPMI, true,
							word, getTopContextAtBeginningAndEnd(db, word)));
			model.put("frequencydata",
					getFrequencyJSON(db, DatabaseService.TEST_FREQUENCY, word));
			return new ModelAndView(model, "result");
		}, new ThymeleafTemplateEngine());

		get("/api/similarity", (request, response) -> {
			String word1 = request.queryParams("word1");
			String word2 = request.queryParams("word2");
			return getAssociationJSON(db, DatabaseService.TEST_SIMILARITY,
					false, word1, word2);
		}, new Gson()::toJson);

		get("/api/ppmi", (request, response) -> {
			String word1 = request.queryParams("word1");
			String word2 = request.queryParams("word2");
			return getAssociationJSON(db, DatabaseService.TEST_PPMI, true,
					word1, word2);
		}, new Gson()::toJson);

		get("/api/frequency", (request, response) -> {
			String word = request.queryParams("word");
			return getFrequencyJSON(db, DatabaseService.TEST_FREQUENCY, word);
		}, new Gson()::toJson);
	}

	static final String[] getMostSimilarAtBeginningAndEnd(DatabaseService db,
			String word) throws Exception {
		List<Integer> years = db.getYears(word);
		Set<String> mostSimilar = new HashSet<>();
		mostSimilar.addAll(
				db.getMostSimilarWordsInYear(word, years.get(0), LIMIT));
		mostSimilar.addAll(db.getMostSimilarWordsInYear(word,
				years.get(years.size() - 1), LIMIT));
		return mostSimilar.toArray(new String[mostSimilar.size()]);
	}

	static final String[] getTopContextAtBeginningAndEnd(DatabaseService db,
			String word) throws Exception {
		List<Integer> years = db.getYears(word);
		Set<String> topContext = new HashSet<>();
		topContext.addAll(db.getTopContextWordsInYear(DatabaseService.TEST_PPMI,
				word, years.get(0), LIMIT));
		topContext.addAll(db.getTopContextWordsInYear(DatabaseService.TEST_PPMI,
				word, years.get(years.size() - 1), LIMIT));
		return topContext.toArray(new String[topContext.size()]);
	}

	//TODO: functional interface to merge both?
	static final Map<String, Object> getAssociationJSON(DatabaseService db,
			String table, boolean directed, String initialWord,
			String... moreWords) throws Exception {
		JSON data = new JSON();

		for (String word : moreWords) {
			data.addValues(word, db.getYearAndAssociation(table, directed,
					initialWord, word));
		}

		return data.data;
	}

	static final Map<String, Object> getFrequencyJSON(DatabaseService db,
			String table, String word) throws Exception {
		return new JSON().addValues(word,
				db.getYearAndFrequencyn(table, word)).data;
	}

}
