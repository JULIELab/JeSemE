import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Compromise between Thymeleaf JSON serialization and convenience
 * @author hellrich
 *
 */

public class JSON {
	
	final Map<String, Object> data = new HashMap<>();
	final HashMap<String, String> xs = new HashMap<>();
	final ArrayList<Object> columns = new ArrayList<>();
	
	
	public JSON(){
		data.put("xs", xs);
		data.put("columns", columns);
	}
	
}
