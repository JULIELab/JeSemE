import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;
import static spark.Spark.*;

import com.google.gson.Gson;

import database.Query;

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
		Query q = new Query();
		get("/json", (request, response) -> ProofOfConcept.getJSON(q, request,
				response), gson::toJson);
		

	}

	static final HashMap<String, Object[]> getJSON(Query q, Request request,
			Response response) throws Exception {
		HashMap<String, Object[]> fo = new HashMap<>();
		Object[] bar = new Object[] { q.getSimilarity("bar","foo") };
		fo.put("columns", bar);
		return fo;
	}
	
}