package helper;

import org.sql2o.Sql2o;

import com.zaxxer.hikari.HikariDataSource;

import configuration.Configuration;
import database.DatabaseService;

public class DatabaseServiceHelper {
	private DatabaseService db = null;
	private HikariDataSource ds = null;
	
	public DatabaseServiceHelper() throws Exception {
		final Configuration config = Configuration
				.readYamlFile("src/test/resources/config.yaml");
		ds = new HikariDataSource();
		ds.setJdbcUrl(config.getDatabase().getUrl());
		ds.setUsername(config.getDatabase().getUser());
		ds.setPassword(config.getDatabase().getPassword());

		final Sql2o sql2o = new Sql2o(ds);

		DatabaseService.initializeTables(sql2o);
		DatabaseService.importTables(config, sql2o);
		db = new DatabaseService(sql2o, config);
	}
	
	public void after() {
		db.dropAll();
		ds.close();
	}

	public DatabaseService getDatabaseService() throws Exception {
		return db;
	}
}
