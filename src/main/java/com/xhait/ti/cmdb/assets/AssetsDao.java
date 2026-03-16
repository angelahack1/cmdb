package com.xhait.ti.cmdb.assets;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.xhait.ti.cmdb.mongo.MongoClientProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DAO for the "Assets" collection.
 *
 * Document shape used by this demo:
 * { _id: ObjectId, name: String, type: String, owner: String, createdAt: Date }
 */
public class AssetsDao {
	private static final Logger log = LogManager.getLogger(AssetsDao.class);
	public static final String COLLECTION_NAME = "Assets";

	private final MongoDatabase db;

	public AssetsDao() {
		log.debug("ENTER AssetsDao() default");
		this.db = MongoClientProvider.getDatabase();
		log.debug("EXIT AssetsDao() default");
	}

	public AssetsDao(MongoDatabase db) {
		log.debug("ENTER AssetsDao(db={})", db);
		this.db = db;
		log.debug("EXIT AssetsDao(db)");
	}

	private MongoCollection<Document> collection() {
		log.debug("ENTER collection()");
		MongoCollection<Document> c = db.getCollection(COLLECTION_NAME);
		log.debug("EXIT collection() -> {}", c.getNamespace());
		return c;
	}

	public List<Asset> listLatest(int limit) {
		log.debug("ENTER listLatest(limit={})", limit);
		int effectiveLimit = Math.max(1, Math.min(limit, 200));
		if (effectiveLimit != limit) {
			log.debug("Adjusted limit {} -> {}", limit, effectiveLimit);
		}
		List<Asset> out = new ArrayList<>();
		int count = 0;
		for (Document d : collection().find().sort(Sorts.descending("createdAt")).limit(effectiveLimit)) {
			out.add(fromDocument(d));
			count++;
		}
		log.info("Listed {} assets (limit={})", count, effectiveLimit);
		log.debug("EXIT listLatest() size={}", out.size());
		return out;
	}

	public Asset insert(String name, String type, String owner) {
		log.debug("ENTER insert(name='{}', type='{}', owner='{}')", name, type, owner);
		if (isBlank(name)) {
			log.warn("Insert rejected: blank name");
			throw new IllegalArgumentException("name is required");
		}

		Document doc = new Document();
		doc.append("name", name.trim());
		doc.append("type", isBlank(type) ? null : type.trim());
		doc.append("owner", isBlank(owner) ? null : owner.trim());
		doc.append("createdAt", java.util.Date.from(Instant.now()));

		collection().insertOne(doc);
		Asset a = fromDocument(doc);
		log.info("Inserted asset id={} name='{}'", a.getId(), a.getName());
		log.debug("EXIT insert() -> id={}", a.getId());
		return a;
	}

	public boolean deleteById(String id) {
		log.debug("ENTER deleteById(id={})", id);
		if (isBlank(id)) {
			log.debug("EXIT deleteById -> false (blank id)");
			return false;
		}
		ObjectId oid;
		try {
			oid = new ObjectId(id);
		} catch (IllegalArgumentException ex) {
			log.warn("Invalid ObjectId '{}'", id);
			log.debug("EXIT deleteById -> false (invalid ObjectId)");
			return false;
		}
		long deleted = collection().deleteOne(Filters.eq("_id", oid)).getDeletedCount();
		boolean ok = deleted > 0;
		log.info("Delete asset id={} deletedCount={}", id, deleted);
		log.debug("EXIT deleteById -> {}", ok);
		return ok;
	}

	private static Asset fromDocument(Document d) {
		log.debug("ENTER fromDocument()");
		ObjectId oid = d.getObjectId("_id");
		java.util.Date createdAt = d.getDate("createdAt");
		Asset a = new Asset(
			oid != null ? oid.toHexString() : null,
			d.getString("name"),
			d.getString("type"),
			d.getString("owner"),
			createdAt != null ? createdAt.toInstant() : null);
		log.debug("EXIT fromDocument() id={}", a.getId());
		return a;
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
