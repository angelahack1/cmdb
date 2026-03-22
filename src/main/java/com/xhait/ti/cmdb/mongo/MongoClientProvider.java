package com.xhait.ti.cmdb.mongo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Lazily creates and reuses a single MongoClient.
 *
 * Configuration order:
 * 1) Explicit environment variables: MONGODB_URI, MONGODB_DB
 * 2) Explicit system properties: mongodb.uri, mongodb.db
 * 3) Separate host/auth settings: MONGODB_HOST, MONGODB_PORT, MONGODB_USER,
 *    MONGODB_PASSWORD, MONGODB_AUTH_DB (or matching system properties)
 * 4) Local demo defaults: localhost:27017, cmdb, cmdbApp/changeme
 */
public final class MongoClientProvider {
	private static final Logger log = LogManager.getLogger(MongoClientProvider.class);
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PORT = "27017";
	private static final String DEFAULT_DATABASE = "cmdb";
	private static final String DEFAULT_USERNAME = "cmdbApp";
	private static final String DEFAULT_PASSWORD = "changeme";

	private static volatile MongoClient client;

	private MongoClientProvider() {
		log.debug("ENTER MongoClientProvider() ctor");
		log.debug("EXIT MongoClientProvider() ctor");
	}

	public static MongoClient getClient() {
		log.debug("ENTER getClient()");
		MongoClient existing = client;
		if (existing != null) {
			log.debug("EXIT getClient() -> cached");
			return existing;
		}
		synchronized (MongoClientProvider.class) {
			if (client == null) {
				log.info("Creating MongoClient (first use)");
				client = createClient();
			}
			log.debug("EXIT getClient() -> created");
			return client;
		}
	}

	public static MongoDatabase getDatabase() {
		log.debug("ENTER getDatabase()");
		MongoDatabase db = getClient().getDatabase(getDatabaseName());
		log.debug("EXIT getDatabase() -> {}", db.getName());
		return db;
	}

	public static String getDatabaseName() {
		log.debug("ENTER getDatabaseName()");
		String db = resolveDatabaseName(System.getenv("MONGODB_DB"), System.getProperty("mongodb.db"));
		log.info("Mongo DB name resolved: {}", db);
		log.debug("EXIT getDatabaseName() -> {}", db);
		return db;
	}

	private static MongoClient createClient() {
		log.debug("ENTER createClient()");
		String uri = resolveConnectionUri(
				System.getenv("MONGODB_URI"),
				System.getProperty("mongodb.uri"),
				System.getenv("MONGODB_HOST"),
				System.getProperty("mongodb.host"),
				System.getenv("MONGODB_PORT"),
				System.getProperty("mongodb.port"),
				System.getenv("MONGODB_DB"),
				System.getProperty("mongodb.db"),
				System.getenv("MONGODB_USER"),
				System.getProperty("mongodb.user"),
				System.getenv("MONGODB_PASSWORD"),
				System.getProperty("mongodb.password"),
				System.getenv("MONGODB_AUTH_DB"),
				System.getProperty("mongodb.authDb"));

		log.info("MongoDB URI resolved (redacted)");
		ConnectionString cs = new ConnectionString(uri);
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
		MongoClient c = MongoClients.create(settings);
		log.debug("EXIT createClient()");
		return c;
	}

	static String resolveDatabaseName(String envDb, String propertyDb) {
		return firstNonBlank(envDb, propertyDb, DEFAULT_DATABASE);
	}

	static String resolveConnectionUri(
			String envUri,
			String propertyUri,
			String envHost,
			String propertyHost,
			String envPort,
			String propertyPort,
			String envDb,
			String propertyDb,
			String envUser,
			String propertyUser,
			String envPassword,
			String propertyPassword,
			String envAuthDb,
			String propertyAuthDb) {
		String explicitUri = firstNonBlank(envUri, propertyUri);
		if (!isBlank(explicitUri)) {
			return explicitUri.trim();
		}

		String db = resolveDatabaseName(envDb, propertyDb);
		String host = firstNonBlank(envHost, propertyHost, DEFAULT_HOST);
		String port = firstNonBlank(envPort, propertyPort, DEFAULT_PORT);
		String user = firstNonBlank(envUser, propertyUser, DEFAULT_USERNAME);
		String password = firstNonBlank(envPassword, propertyPassword, DEFAULT_PASSWORD);
		String authDb = firstNonBlank(envAuthDb, propertyAuthDb, db);
		return buildConnectionUri(host, port, db, user, password, authDb);
	}

	static String buildConnectionUri(String host, String port, String db, String user, String password, String authDb) {
		String safeHost = firstNonBlank(host, DEFAULT_HOST);
		String safePort = firstNonBlank(port, DEFAULT_PORT);
		String safeDb = firstNonBlank(db, DEFAULT_DATABASE);
		String safeUser = firstNonBlank(user, DEFAULT_USERNAME);
		String safePassword = firstNonBlank(password, DEFAULT_PASSWORD);
		String safeAuthDb = firstNonBlank(authDb, safeDb);

		return "mongodb://"
				+ urlEncode(safeUser)
				+ ":"
				+ urlEncode(safePassword)
				+ "@"
				+ safeHost
				+ ":"
				+ safePort
				+ "/"
				+ safeDb
				+ "?authSource="
				+ safeAuthDb;
	}

	private static String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (!isBlank(value)) {
				return value.trim();
			}
		}
		return null;
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}