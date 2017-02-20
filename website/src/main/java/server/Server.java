package server;

import static spark.Spark.get;
import static spark.Spark.redirect;
import static spark.Spark.staticFileLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import configuration.Configuration;
import database.DatabaseService;
import server.JSON;
import spark.ModelAndView;
import spark.Request;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Server {
	private static final int LIMIT = 2;

	private static Map<String, Object> getAssociation(final Request request,
			final DatabaseService db, final String table,
			final boolean isContextQuery) throws Exception {
		final String corpus = request.queryParams("corpus");
		final String word1 = request.queryParams("word1");
		final String word2 = request.queryParams("word2");
		return getAssociationJSON(db, corpus, table, isContextQuery, word1,
				word2);
	}

	//TODO: functional interface to merge both?
	static final Map<String, Object> getAssociationJSON(
			final DatabaseService db, final String corpus, final String table,
			final boolean isContextQuery, final String initialWord,
			final String... moreWords) throws Exception {
		final JSON data = new JSON();
		for (final String word : moreWords)
			data.addValues(word, db.getYearAndAssociation(corpus, table,
					isContextQuery, initialWord, word));
		return data.data;
	}

	static final Map<String, Object> getFrequencyJSON(final DatabaseService db,
			final String corpus, final String word) throws Exception {
		return new JSON().addValues(word,
				db.getYearAndFrequency(corpus, word)).data;
	}

	static final String[] getMostSimilarAtBeginningAndEnd(
			final DatabaseService db, final String corpus, final String word)
			throws Exception {
		final List<Integer> years = db.getYears(corpus, word);
		final Set<String> mostSimilar = new HashSet<>();
		mostSimilar.addAll(db.getMostSimilarWordsInYear(corpus, word,
				years.get(0), LIMIT));
		mostSimilar.addAll(db.getMostSimilarWordsInYear(corpus, word,
				years.get(years.size() - 1), LIMIT));
		return mostSimilar.toArray(new String[mostSimilar.size()]);
	}

	static final String[] getTopContextAtBeginningAndEnd(
			final DatabaseService db, final String table, final String corpus,
			final String word) throws Exception {
		final List<Integer> years = db.getYears(corpus, word);
		final Set<String> topContext = new HashSet<>();
		topContext.addAll(db.getTopContextWordsInYear(corpus, table, word,
				years.get(0), LIMIT));
		topContext.addAll(db.getTopContextWordsInYear(corpus, table, word,
				years.get(years.size() - 1), LIMIT));
		return topContext.toArray(new String[topContext.size()]);
	}

	public static void startServer(final DatabaseService db,
			final Configuration config) {
		if (config.coversServer()) {
			spark.Spark.ipAddress(config.getServer().getIp());
			spark.Spark.port(config.getServer().getPort());
		}

		staticFileLocation("/public");
		redirect.get("/", "/index.html");

		get("/search", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			final Map<String, Object> model = new HashMap<>();

			model.put("word", word);
			model.put("corpus", corpus);

			if (db.wordInCorpus(word, corpus)) {
				long t = System.currentTimeMillis();
				final String[] mostSimilar = getMostSimilarAtBeginningAndEnd(db,
						corpus, word);
				System.out.println("most " + (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();

				model.put("similaritydata",
						getAssociationJSON(db, corpus,
								DatabaseService.SIMILARITY_TABLE, false, word,
								mostSimilar));
				System.out.println("sim " + (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();
				model.put("ppmidata",
						getAssociationJSON(db, corpus,
								DatabaseService.PPMI_TABLE, true, word,
								getTopContextAtBeginningAndEnd(db,
										DatabaseService.PPMI_TABLE, corpus,
										word)));
				model.put("chidata",
						getAssociationJSON(db, corpus,
								DatabaseService.CHI_TABLE, true, word,
								getTopContextAtBeginningAndEnd(db,
										DatabaseService.CHI_TABLE, corpus,
										word)));
				System.out.println("ppmi " + (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();
				model.put("frequencydata", getFrequencyJSON(db, corpus, word));
				System.out.println("freq " + (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();
				return new ModelAndView(model, "result");
			} else
				return new ModelAndView(model, "unknown");
		}, new ThymeleafTemplateEngine());

		get("/api/similarity",
				(request, response) -> getAssociation(request, db,
						DatabaseService.SIMILARITY_TABLE, false),
				new Gson()::toJson);

		get("/api/ppmi", (request, response) -> getAssociation(request, db,
				DatabaseService.PPMI_TABLE, true), new Gson()::toJson);

		get("/api/chi", (request, response) -> getAssociation(request, db,
				DatabaseService.CHI_TABLE, true), new Gson()::toJson);

		get("/api/frequency", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			return getFrequencyJSON(db, corpus, word);
		}, new Gson()::toJson);
	}

}
