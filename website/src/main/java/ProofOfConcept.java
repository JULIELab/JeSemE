import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.*;
import com.google.gson.Gson;

public class ProofOfConcept {

    public static void main(String[] args) {

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
        get("/say/*/to/*", (request, response) -> {
            return Arrays.stream(request.splat()).collect(Collectors.joining(" to "));
        });
        Gson gson = new Gson();
        HashMap<String, Object[]> fo = new HashMap<>();
        Object[] bar = new Object[]{new Object[]{"data3",400,11,33}};

        fo.put("columns",bar);
        get("/json", (request, response) -> fo, gson::toJson);

//        "{\n" +
//                "            columns: [\n" +
//                "                ['data3', 400, 250, 150, 200, 100, 350]\n" +
//                "            ]\n" +
//                "        }"
    }
}