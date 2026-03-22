package com.xhait.ti.cmdb.mongo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MongoClientProviderTest {

	@Test
	public void resolveDatabaseNameFallsBackToCmdb() {
		assertEquals("cmdb", MongoClientProvider.resolveDatabaseName(null, null));
	}

	@Test
	public void resolveConnectionUriPrefersExplicitUri() {
		String uri = MongoClientProvider.resolveConnectionUri(
				"mongodb://explicit-host:27017/customdb",
				null,
				"ignored-host",
				null,
				"9999",
				null,
				"ignored-db",
				null,
				"ignored-user",
				null,
				"ignored-password",
				null,
				"ignored-auth-db",
				null);

		assertEquals("mongodb://explicit-host:27017/customdb", uri);
	}

	@Test
	public void resolveConnectionUriBuildsAuthenticatedDemoDefaults() {
		String uri = MongoClientProvider.resolveConnectionUri(
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null);

		assertEquals("mongodb://cmdbApp:changeme@localhost:27017/cmdb?authSource=cmdb", uri);
	}

	@Test
	public void buildConnectionUriEncodesCredentials() {
		String uri = MongoClientProvider.buildConnectionUri(
				"mongo.internal",
				"27018",
				"cmdb",
				"cmdb app",
				"change/me@now",
				"admin");

		assertEquals("mongodb://cmdb%20app:change%2Fme%40now@mongo.internal:27018/cmdb?authSource=admin", uri);
	}
}
