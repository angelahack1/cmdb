package com.xhait.ti.cmdb.assets;

import java.time.Instant;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Simple POJO representation of an Asset document. */
public class Asset {
	private static final Logger log = LogManager.getLogger(Asset.class);

	private String id;
	private String name;
	private String type;
	private String owner;
	private Instant createdAt;

	public Asset() {
		log.debug("ENTER Asset() ctor");
		log.debug("EXIT Asset() ctor");
	}

	public Asset(String id, String name, String type, String owner, Instant createdAt) {
		log.debug("ENTER Asset(id={}, name={}, type={}, owner={}, createdAt={})", id, name, type, owner, createdAt);
		this.id = id;
		this.name = name;
		this.type = type;
		this.owner = owner;
		this.createdAt = createdAt;
		log.debug("EXIT Asset(...) ctor");
	}

	public String getId() {
		log.debug("ENTER getId()");
		log.debug("EXIT getId() -> {}", id);
		return id;
	}

	public void setId(String id) {
		log.debug("ENTER setId({})", id);
		this.id = id;
		log.debug("EXIT setId()");
	}

	public String getName() {
		log.debug("ENTER getName()");
		log.debug("EXIT getName() -> {}", name);
		return name;
	}

	public void setName(String name) {
		log.debug("ENTER setName({})", name);
		this.name = name;
		log.debug("EXIT setName()");
	}

	public String getType() {
		log.debug("ENTER getType()");
		log.debug("EXIT getType() -> {}", type);
		return type;
	}

	public void setType(String type) {
		log.debug("ENTER setType({})", type);
		this.type = type;
		log.debug("EXIT setType()");
	}

	public String getOwner() {
		log.debug("ENTER getOwner()");
		log.debug("EXIT getOwner() -> {}", owner);
		return owner;
	}

	public void setOwner(String owner) {
		log.debug("ENTER setOwner({})", owner);
		this.owner = owner;
		log.debug("EXIT setOwner()");
	}

	public Instant getCreatedAt() {
		log.debug("ENTER getCreatedAt()");
		log.debug("EXIT getCreatedAt() -> {}", createdAt);
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		log.debug("ENTER setCreatedAt({})", createdAt);
		this.createdAt = createdAt;
		log.debug("EXIT setCreatedAt()");
	}

	@Override
	public int hashCode() {
		log.debug("ENTER hashCode()");
		int hc = Objects.hash(id, name, type, owner, createdAt);
		log.debug("EXIT hashCode() -> {}", hc);
		return hc;
	}

	@Override
	public boolean equals(Object obj) {
		log.debug("ENTER equals(obj={})", obj);
		if (this == obj) {
			log.debug("EXIT equals -> true (same ref)");
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			log.debug("EXIT equals -> false (null/different class)");
			return false;
		}
		Asset other = (Asset) obj;
		boolean eq = Objects.equals(id, other.id) && Objects.equals(name, other.name) && Objects.equals(type, other.type)
				&& Objects.equals(owner, other.owner) && Objects.equals(createdAt, other.createdAt);
		log.debug("EXIT equals -> {}", eq);
		return eq;
	}
}