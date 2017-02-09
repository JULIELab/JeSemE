import com.google.gson.Gson;

import configuration.Configuration;
import database.DatabaseService;

import org.dbunit.DatabaseUnitException;
import org.docopt.Docopt;
import org.sql2o.Sql2o;
import org.yaml.snakeyaml.Yaml;

import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.io.FileReader;
import java.util.*;
import static spark.Spark.*;

public class ProofOfConcept {

	private static final int LIMIT = 2;
	private static final String doc =
		    "JeDiSem\n"
		    + "Usage:\n"
		    + "  jedisem server <dbconfig>"
		    + "  jedisem import <dbconfig>"
		    + "  jedisem initialize <dbconfig>"
		    + "\n"
		    + "Options:\n"
		    + "  -h --help     Show this screen.\n"
		    + "  --version     Show version.\n"
		    + "\n";

	public static void main(String[] args) throws Exception {
		Map<String, Object> opts =
		        new Docopt(doc).withVersion("Naval Fate 2.0").parse(args);
		Configuration c = Configuration.readYamlFile(opts.get("<dbconfig>").toString()); //TODO: unused so far
		if((boolean) opts.get("server"))
			startServer(c);
		else if((boolean) opts.get("import"))
			importDatabase(c);
		else if ((boolean) opts.get("initialize"))
			initializeDatabase(c);
	}

	private static void initializeDatabase(Configuration c) {
		// TODO Auto-generated method stub
		
	}

	private static void importDatabase(Configuration c) {
		// TODO Auto-generated method stub
		
	}

	private static void startServer(Configuration c) throws DatabaseUnitException, Exception {
		Sql2o sql2o = new Sql2o(c.getDatabase().getUrl(),c.getDatabase().getUser(),c.getDatabase().getPassword());
		//TODO: save when multiple connections are made? move into database service?
		DatabaseService db = new DatabaseService(sql2o, "/Users/hellrich/Desktop/demo1000"); 
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
