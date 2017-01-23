import com.google.gson.Gson;

import database.DatabaseService;
import database.YearAndSimilarity;

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

		get("/search",
				(request, response) -> {
					String word = request.queryParams("word");
					
					Map<String, Object> model = new HashMap<>();
					model.put("word", word);
					model.put("similaritydata",
							getJSON(db, word,
									getMostSimilarAtBeginningAndEnd(db, word)));
					return new ModelAndView(model, "result");
				}, new ThymeleafTemplateEngine());

		get("/api/similarity", (request, response) -> {
			String word1 = request.queryParams("word1");
			String word2 = request.queryParams("word2");
			return getJSON(db, word1, word2);
		}, new Gson()::toJson);

		//TODO: replace with real implementation
		get("/api/ppmi", (request, response) -> {
			String word1 = request.queryParams("word1");
			String word2 = request.queryParams("word2");
			return getJSON(db, word1, word2);
		}, new Gson()::toJson);
	}

	static final String[] getMostSimilarAtBeginningAndEnd(DatabaseService db,
			String word) throws Exception {
		List<Integer> years = db.getYears(word);
		Set<String> mostSimilar = new HashSet<>();
		mostSimilar.addAll(db.getMostSimilarWordsInYear(word, years.get(0),
				LIMIT));
		mostSimilar.addAll(db.getMostSimilarWordsInYear(word,
				years.get(years.size() - 1), LIMIT));
		return mostSimilar.toArray(new String[mostSimilar.size()]);
	}

	static final HashMap<String, Object> getJSON(DatabaseService db,
			String initialWord, String... moreWords) throws Exception {

		HashMap<String, String> xs = new HashMap<>();
		ArrayList<Object> columns = new ArrayList<>();
		for (String word : moreWords) {
			List<Object> simList = new ArrayList<>();
			List<Object> xList = new ArrayList<>();
			xs.put(word, word + "-x-value");
			simList.add(word);
			xList.add(word + "-x-value");

			for (YearAndSimilarity yas : db.getYearAndSimilarity(initialWord,
					word)) {
				simList.add(yas.similarity);
				xList.add(yas.year);
			}
			columns.add(simList);
			columns.add(xList);
		}

		HashMap<String, Object> data = new HashMap<>();
		data.put("xs", xs);
		data.put("columns", columns);

		return data;
	}

}
