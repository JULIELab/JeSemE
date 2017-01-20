import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.sql2o.Sql2o;
import org.sql2o.logging.SysOutLogger;

import spark.Request;
import spark.Response;
import static spark.Spark.*;

import com.google.gson.Gson;

import database.Query;
import database.YearAndSimilarity;

public class ProofOfConcept {

	public static void main(String[] args) throws Exception {

		staticFileLocation("/public");
		redirect.get("/", "/index.html");

		get("/hello", (req, res) -> "hello");

		// matches "GET /hello/foo" and "GET /hello/bar"
		// request.params(":name") is 'foo' or 'bar'
		get("/hello/:name", (request, response) -> {
			return "Hello: " + request.params(":name");
		});

		// matches "GET /say/hello/to/world"
		// request.splat()[0] is 'hello' and request.splat()[1] 'world'
		get("/say/*/to/*",
				(request, response) -> {
					return Arrays.stream(request.splat()).collect(
							Collectors.joining(" to "));
				});
		Gson gson = new Gson();
		Class.forName("org.hsqldb.jdbcDriver");
		Sql2o sql2o = new Sql2o("jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
		Query q = new Query(sql2o);
		get("/json", (request, response) -> ProofOfConcept.getJSON(q, request,
				response), gson::toJson);
		
		 Map map = new HashMap();
        	 map.put("name", "Sam");

        // TODO: replace map, test with static data, test reading of parameters
                 get("/result", (rq, rs) -> new ModelAndView(map, "hello"), new ThymeleafTemplateEngine());
		
//		final Connection connection = DriverManager.getConnection(
//				"jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");

	}

	static final HashMap<String, Object> getJSON(Query q, Request request,
			Response response) throws Exception {
		
		HashMap<String,String> xs = new HashMap<>();
		ArrayList<Object> columns = new ArrayList<>();
		for(String word : new String[]{"bar", "arr", "boo"}){
			List<Object> simList = new ArrayList<>();
			List<Object> xList = new ArrayList<>();
			xs.put(word, word+"-x-value");
			simList.add(word);
			xList.add(word+"-x-value");
			
			for(YearAndSimilarity yas : q.getYearAndSimilarity(word,"foo")){
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
