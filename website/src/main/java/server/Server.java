package server;

import static spark.Spark.get;
import static spark.Spark.redirect;
import static spark.Spark.staticFileLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import configuration.Configuration;
import database.DatabaseService;
import spark.ModelAndView;
import spark.Request;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Server {
	private static final int LIMIT = 2;
	private static final ExecutorService executor = Executors
			.newFixedThreadPool(20);

	private static Map<String, Object> getAssociation(final Request request,
			final DatabaseService db, final String table,
			final boolean isContextQuery) throws Exception {
		final String corpus = request.queryParams("corpus");
		final String word1 = request.queryParams("word1");
		final String word2 = request.queryParams("word2");
		return getAssociationJson(db, corpus, table, isContextQuery, word1,
				word2);
	}

	static final Map<String, Object> getAssociationJson(
			final DatabaseService db, final String corpus, final String table,
			final boolean isContextQuery, final String initialWord,
			final String... moreWords) throws Exception {
		final JSON data = new JSON();
		for (final String word : moreWords)
			data.addValues(word, db.getYearAndAssociation(corpus, table,
					isContextQuery, initialWord, word));
		return data.data;
	}

	static final Map<String, Object> getFrequencyJson(final DatabaseService db,
			final String corpus, final String word) throws Exception {
		return new JSON().addValues(word,
				db.getYearAndFrequency(corpus, word)).data;
	}

	private static Callable<List<String>> getMostSimilarAsync(
			final boolean first, final DatabaseService db, final String corpus,
			final String word) throws Exception {
		return () -> db.getMostSimilarWordsInYear(corpus, word, first
				? db.getFirstYear(corpus, word) : db.getLastYear(corpus, word),
				LIMIT);
	}

	static final String[] getMostSimilarAtBeginningAndEnd(
			final DatabaseService db, final String corpus, final String word)
			throws Exception {
		return executor
				.invokeAll(Arrays.asList(
						getMostSimilarAsync(true, db, corpus, word),
						getMostSimilarAsync(false, db, corpus, word)))
				.stream().map(future -> {
					try {
						return future.get();
					} catch (final Exception e) {
						throw new IllegalStateException(e);
					}
				}).flatMap(List::stream).distinct().toArray(String[]::new);
	}

	static final String[] getTopContextAtBeginningAndEnd(
			final DatabaseService db, final String table, final String corpus,
			final String word) throws Exception {
		final Set<String> topContext = new HashSet<>();
		topContext.addAll(db.getTopContextWordsInYear(corpus, table, word,
				db.getFirstYear(corpus, word), LIMIT));
		topContext.addAll(db.getTopContextWordsInYear(corpus, table, word,
				db.getLastYear(corpus, word), LIMIT));
		return topContext.toArray(new String[topContext.size()]);
	}

	public static void startServer(final DatabaseService db,
			final Configuration config) {
		if (config.coversServer()) {
			spark.Spark.ipAddress(config.getServer().getIp());
			spark.Spark.port(config.getServer().getPort());
			System.out.println("Using " + config.getServer().getIp() + ":"
					+ config.getServer().getPort());
		}

		staticFileLocation("/public");
		redirect.get("/", "/index.html");

		get("/search", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			final Map<String, Object> model = new HashMap<>();

			model.put("word", word);
			model.put("corpus", corpus);
			model.put("corpusName", db.getCorpusName(corpus));
			model.put("corpusNote", db.getCorpusNote(corpus));
			model.put("corpusLink", db.getCorpusLink(corpus, word));

			if (db.wordInCorpus(word, corpus))
				return new ModelAndView(model, "result");
			else if (db.knowCorpus(corpus))
				return new ModelAndView(model, "unknownWord");
			else
				return new ModelAndView(model, "unknownCorpus");
		}, new ThymeleafTemplateEngine());

		get("/api/mostsimilar", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			final String[] mostSimilar = getMostSimilarAtBeginningAndEnd(db,
					corpus, word);
			return getAssociationJson(db, corpus,
					DatabaseService.SIMILARITY_TABLE, false, word, mostSimilar);
		}, new Gson()::toJson);

		get("/api/typicalcontextppmi", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			return getAssociationJson(db, corpus, DatabaseService.PPMI_TABLE,
					true, word, getTopContextAtBeginningAndEnd(db,
							DatabaseService.PPMI_TABLE, corpus, word));
		}, new Gson()::toJson);

		get("/api/typicalcontextchi", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			return getAssociationJson(db, corpus, DatabaseService.CHI_TABLE,
					true, word, getTopContextAtBeginningAndEnd(db,
							DatabaseService.CHI_TABLE, corpus, word));
		}, new Gson()::toJson);

		get("/api/similarity",
				(request, response) -> getAssociation(request, db,
						DatabaseService.SIMILARITY_TABLE, false),
				new Gson()::toJson);

		get("/api/ppmi", (request, response) -> getAssociation(request, db,
				DatabaseService.PPMI_TABLE, true), new Gson()::toJson);

		get("/api/chi", (request, response) -> getAssociation(request, db,
				DatabaseService.CHI_TABLE, true), new Gson()::toJson);

		get("/api/covers", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			final Map<String, Boolean> answer = new HashMap<>();
			answer.put("covers", db.wordInCorpus(word, corpus));
			return answer;
		}, new Gson()::toJson);

		get("/api/frequency", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			return getFrequencyJson(db, corpus, word);
		}, new Gson()::toJson);
	}

	public static void stopServer() {
		spark.Spark.stop();
	}

}
