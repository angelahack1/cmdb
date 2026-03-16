package com.xhait.ti.cmdb.mongo;

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
 * 1) Environment variables: MONGODB_URI, MONGODB_DB
 * 2) System properties: mongodb.uri, mongodb.db
 */
public final class MongoClientProvider {
	private static final Logger log = LogManager.getLogger(MongoClientProvider.class);
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
		String db = firstNonBlank(System.getenv("MONGODB_DB"), System.getProperty("mongodb.db"));
		if (isBlank(db)) {
			log.error("MongoDB database name not configured (MONGODB_DB / mongodb.db)");
			throw new IllegalStateException(
					"MongoDB database name is not configured. Set env MONGODB_DB or system property mongodb.db.");
		}
		log.info("Mongo DB name configured: {}", db);
		log.debug("EXIT getDatabaseName() -> {}", db);
		return db;
	}

	private static MongoClient createClient() {
		log.debug("ENTER createClient()");
		String uri = firstNonBlank(System.getenv("MONGODB_URI"), System.getProperty("mongodb.uri"));
		if (isBlank(uri)) {
			log.error("MongoDB URI not configured (MONGODB_URI / mongodb.uri)");
			throw new IllegalStateException(
					"MongoDB connection URI is not configured. Set env MONGODB_URI or system property mongodb.uri.");
		}

		log.info("MongoDB URI configured (redacted)");
		ConnectionString cs = new ConnectionString(uri);
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
		MongoClient c = MongoClients.create(settings);
		log.debug("EXIT createClient()");
		return c;
	}

	private static String firstNonBlank(String a, String b) {
		return !isBlank(a) ? a : b;
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
