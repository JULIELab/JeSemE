package de.julielab.jeseme.server;

import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.redirect;
import static spark.Spark.staticFileLocation;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

import de.julielab.jeseme.configuration.Configuration;
import de.julielab.jeseme.database.DatabaseService;
import de.julielab.jeseme.database.YearAndValue;
import de.julielab.jeseme.embeddings.Embedding;
import spark.ModelAndView;
import spark.Request;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Server {
	private static final Logger LOG = LoggerFactory.getLogger(Server.class);
	private static final int LIMIT = 2; // do not set over 5!
	private static final int THREADS = 12;

	private static void configureServer(final Configuration config) {
		if (config.coversServer()) {
			spark.Spark.ipAddress(config.getServer().getIp());
			spark.Spark.port(config.getServer().getPort());
			System.out.println("Using " + config.getServer().getIp() + ":" + config.getServer().getPort());
		}
		spark.Spark.threadPool(THREADS);
	}

	private static Map<String, Object> getAssociation(final Request request, final DatabaseService db,
			final String table) throws Exception {
		final String corpus = request.queryParams("corpus");
		final String word1 = request.queryParams("word1");
		final String word2 = request.queryParams("word2");
		LOG.info("association {} between {} and {} in {}", new Object[] { table, word1, word2, corpus });
		return getAssociationJson(db, corpus, table, word1, word2);
	}

	static final Map<String, Object> getAssociationJson(final DatabaseService db, final String corpus,
			final String table, final String initialWord, final String... moreWords) throws Exception {
		final JSON data = new JSON();
		for (final String word : moreWords)
			data.addValues(word, db.getYearAndAssociation(corpus, table, initialWord, word));
		LOG.trace("finished association {} with {} in {}", new Object[] { table, initialWord, corpus });
		return data.data;
	}

	static final Map<String, Object> getEmotionJson(final DatabaseService db, final String corpus, final String word)
			throws Exception {
		final JSON data = new JSON();
		final Map<String, List<YearAndValue>> emotion = db.getEmotion(corpus, word);
		for (final String s : emotion.keySet())
			data.addValues(s, emotion.get(s));
		return data.data;
	}

	static final Map<String, Object> getFrequencyJson(final DatabaseService db, final String corpus, final String word)
			throws Exception {
		return new JSON().addValues(word, db.getYearAndFrequency(corpus, word)).data;
	}

	static final Map<String, Object> getSimilarityJson(final DatabaseService db, final String corpus,
			final String initialWord, final String... moreWords) throws Exception {
		final JSON data = new JSON();
		final Map<Integer, Embedding> initialWordEmbeddings = db.getEmbedding(corpus, initialWord);
		for (final String word : moreWords) {
			final Map<Integer, Embedding> otherWordEmbeddings = db.getEmbedding(corpus, word);
			final List<YearAndValue> yavs = new ArrayList<>();
			otherWordEmbeddings.keySet().stream().filter(x -> initialWordEmbeddings.containsKey(x)).sorted()
					.forEach(year -> {
						yavs.add(new YearAndValue(year,
								(float) initialWordEmbeddings.get(year).similarity(otherWordEmbeddings.get(year))));
					});

			data.addValues(word, yavs);
		}
		LOG.trace("finished similarity {} in {}", new Object[] { initialWord, corpus });
		return data.data;
	}

	static final String[] getTopContextAtBeginningAndEnd(final DatabaseService db, final String table,
			final String corpus, final String word) throws Exception {
		final Set<String> topContext = new HashSet<>();
		topContext.addAll(db.getTopContextWordsInYear(corpus, table, word, db.getFirstYear(corpus, word), LIMIT));
		topContext.addAll(db.getTopContextWordsInYear(corpus, table, word, db.getLastYear(corpus, word), LIMIT));
		return topContext.toArray(new String[topContext.size()]);
	}

	public static void startErrorServer(final Configuration config, final ArrayList<String> messageParts) {
		configureServer(config);
		final Map<String, Object> model = new HashMap<>();
		model.put("message", messageParts.stream().collect(Collectors.joining(" ")));
		final ModelAndView modelAndView = new ModelAndView(model, "error");
		get("/", (request, response) -> modelAndView, new ThymeleafTemplateEngine());
	}

	public static void startServer(final DatabaseService db, final Configuration config, boolean onlyExternal) {
		configureServer(config);

		if (!onlyExternal)
			staticFileLocation("/public");
		externalStaticFileLocation("downloads");

		redirect.get("/", "/index.html");

		get("/file", (request, response) -> new FileInputStream("/index.html"));

		get("/search", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			LOG.info("search {} in {}", word, corpus);
			final Map<String, Object> model = new HashMap<>();

			model.put("word", word);
			model.put("corpus", corpus);
			model.put("lineChart", !corpus.equals("rsc"));// TODO move in config
															// or make switch on
															// website?
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
			LOG.info("mostsimilar {} in {}", word, corpus);
			final String[] mostSimilar = getTopContextAtBeginningAndEnd(db, DatabaseService.SIMILARITY_TABLE, corpus,
					word);
			// uses similarity with embeddings
			return getSimilarityJson(db, corpus, word, mostSimilar);
		}, new Gson()::toJson);

		get("/api/typicalcontextppmi", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			LOG.info("typicalcontextppmi {} in {}", word, corpus);
			return getAssociationJson(db, corpus, DatabaseService.PPMI_TABLE, word,
					getTopContextAtBeginningAndEnd(db, DatabaseService.PPMI_TABLE, corpus, word));
		}, new Gson()::toJson);

		get("/api/typicalcontextchi", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			LOG.info("typicalcontextchi {} in {}", word, corpus);
			return getAssociationJson(db, corpus, DatabaseService.CHI_TABLE, word,
					getTopContextAtBeginningAndEnd(db, DatabaseService.CHI_TABLE, corpus, word));
		}, new Gson()::toJson);

		get("/api/similarity", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word1 = request.queryParams("word1");
			final String word2 = request.queryParams("word2");
			LOG.info("similarity between {} and {} in {}", new Object[] { word1, word2, corpus });
			return getSimilarityJson(db, corpus, word1, word2);
		}, new Gson()::toJson);

		get("/api/ppmi", (request, response) -> getAssociation(request, db, DatabaseService.PPMI_TABLE),
				new Gson()::toJson);

		get("/api/chi", (request, response) -> getAssociation(request, db, DatabaseService.CHI_TABLE),
				new Gson()::toJson);

		get("/api/covers", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			LOG.info("covers {} in {}", word, corpus);
			final Map<String, Boolean> answer = new HashMap<>();
			answer.put("covers", db.wordInCorpus(word, corpus));
			return answer;
		}, new Gson()::toJson);

		get("/api/frequency", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			LOG.info("frequency {} in {}", word, corpus);
			return getFrequencyJson(db, corpus, word);
		}, new Gson()::toJson);

		get("/api/emotion", (request, response) -> {
			final String corpus = request.queryParams("corpus");
			final String word = request.queryParams("word");
			LOG.info("emotion {} in {}", word, corpus);
			return getEmotionJson(db, corpus, word);
		}, new Gson()::toJson);
	}

	public static void stopServer() {
		spark.Spark.stop();
	}

}
