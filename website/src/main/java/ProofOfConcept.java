import com.google.gson.Gson;
import database.Query;
import database.YearAndSimilarity;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;
import org.sql2o.logging.SysOutLogger;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class ProofOfConcept {



	public static void main(String[] args) throws Exception {
       // System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

		Sql2o sql2o = new Sql2o("jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
		Query q = new Query(sql2o); //save when multiple connections are made? move into query methods?

		staticFileLocation("/public");
		redirect.get("/", "/index.html");

		get("/search", (request, response) -> {
			String word = request.queryParams("word");

			//get similar words from first and last year
			List<Integer> years = q.getYears(word);
			List<String> otherWords = q.getMostSimilarWordsInYear(word,years.get(0),2);
            List<String> moreWords = q.getMostSimilarWordsInYear(word,years.get(years.size() - 1),2);
            Set<String> words = new HashSet(otherWords);
            words.addAll(moreWords);

			Map map = new HashMap();
			map.put("word", word);
			map.put("similaritydata", getJSON(q,word,words.toArray(new String[words.size()])));
			return new ModelAndView(map, "result");}, new ThymeleafTemplateEngine());

        get("/api/similarity", (request, response) -> {
            String word1 = request.queryParams("word1");
            String word2 = request.queryParams("word2");
            return getJSON(q,word1,word2);}, new Gson()::toJson);

        //TODO: replace with real implementation
        get("/api/ppmi", (request, response) -> {
            String word1 = request.queryParams("word1");
            String word2 = request.queryParams("word2");
            return getJSON(q,word1,word2);}, new Gson()::toJson);
	}


	static final HashMap<String, Object> getJSON(Query q, String word2, String... otherWords) throws Exception {

		HashMap<String,String> xs = new HashMap<>();
		ArrayList<Object> columns = new ArrayList<>();
		for(String word : otherWords){
			List<Object> simList = new ArrayList<>();
			List<Object> xList = new ArrayList<>();
			xs.put(word, word+"-x-value");
			simList.add(word);
			xList.add(word+"-x-value");

			for(YearAndSimilarity yas : q.getYearAndSimilarity(word,word2)){
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
