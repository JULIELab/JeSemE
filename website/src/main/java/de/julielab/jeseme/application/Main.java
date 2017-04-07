package de.julielab.jeseme.application;

import java.util.ArrayList;
import java.util.Map;

import org.docopt.Docopt;
import org.sql2o.Sql2o;

import com.zaxxer.hikari.HikariDataSource;

import de.julielab.jeseme.configuration.Configuration;
import de.julielab.jeseme.database.DatabaseService;
import de.julielab.jeseme.server.Server;

public class Main {
	private static final String doc = "JeDiSem\n" + "Usage:\n"
			+ "  jedisem server <dbconfig>\n" + "  jedisem import <dbconfig>\n"
			+ "  jedisem initialize <dbconfig>\n"
			+ "  jedisem demo <dbconfig>\n"
			+ "  jedisem error <dbconfig> <message>... \n\n" + "Options:\n"
			+ "  -h --help     Show this screen.\n";

	@SuppressWarnings("unchecked")
	public static void main(final String[] args) throws Exception {
		final Map<String, Object> opts = new Docopt(doc).parse(args);
		final Configuration config = Configuration
				.readYamlFile(opts.get("<dbconfig>").toString());

		if ((boolean) opts.get("server"))
			Server.startServer(
					new DatabaseService(prepareSql2o(config), config), config);
		else if ((boolean) opts.get("import"))
			DatabaseService.importTables(config, prepareSql2o(config));
		else if ((boolean) opts.get("initialize"))
			DatabaseService.initializeTables(prepareSql2o(config));
		else if ((boolean) opts.get("demo")) {
			Sql2o sql2o = prepareSql2o(config);
			DatabaseService.initializeTables(sql2o);
			DatabaseService.importTables(config, sql2o);
			Server.startServer(new DatabaseService(sql2o, config), config);
		} else if ((boolean) opts.get("error")) {
			Server.startErrorServer(config, (ArrayList<String>) opts.get("<message>"));
		} else
			throw new IllegalArgumentException();
	}

	private static Sql2o prepareSql2o(Configuration config) {
		final HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(config.getDatabase().getUrl());
		ds.setUsername(config.getDatabase().getUser());
		ds.setPassword(config.getDatabase().getPassword());
		return new Sql2o(ds);
	}

}
