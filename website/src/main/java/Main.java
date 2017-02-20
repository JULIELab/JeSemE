
import static server.Server.startServer;

import java.util.Map;

import org.docopt.Docopt;
import org.sql2o.Sql2o;

import com.zaxxer.hikari.HikariDataSource;

import configuration.Configuration;
import database.DatabaseService;

public class Main {
	private static final String doc = "JeDiSem\n" + "Usage:\n"
			+ "  jedisem server <dbconfig>\n" + "  jedisem import <dbconfig>\n"
			+ "  jedisem initialize <dbconfig>\n"
			+ "  jedisem demo <dbconfig>\n" + "\n" + "Options:\n"
			+ "  -h --help     Show this screen.\n";

	public static void main(final String[] args) throws Exception {
		final Map<String, Object> opts = new Docopt(doc).parse(args);
		final Configuration config = Configuration
				.readYamlFile(opts.get("<dbconfig>").toString());
		final HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(config.getDatabase().getUrl());
		ds.setUsername(config.getDatabase().getUser());
		ds.setPassword(config.getDatabase().getPassword());

		final Sql2o sql2o = new Sql2o(ds);
		if ((boolean) opts.get("server"))
			startServer(new DatabaseService(sql2o, config), config);
		else if ((boolean) opts.get("import"))
			DatabaseService.importTables(config, sql2o);
		else if ((boolean) opts.get("initialize"))
			DatabaseService.initializeTables(sql2o);
		else if ((boolean) opts.get("demo")) {
			DatabaseService.initializeTables(sql2o);
			DatabaseService.importTables(config, sql2o);
			startServer(new DatabaseService(sql2o, config), config);
		}
	}

}
