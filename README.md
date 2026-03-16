# cmdb

A Jakarta Servlet/JSP web application packaged as a WAR and intended to run on Eclipse GlassFish 7. The current codebase is a small CMDB-style starter app centered on an **Assets** feature backed by MongoDB, plus a health endpoint, Log4j2-based logging, and an OWASP Dependency-Check workflow.

---

## What this project is

This repository is **not just a dependency-check setup**.

The actual application in `cmdb/` is a Java 17 web app with:

- a landing page at `index.jsp`
- an `Assets` module for listing and creating MongoDB-backed asset records
- a `/health` endpoint for deployment checks
- Log4j2 file + console logging
- Maven profiles for autodeploy, cleanup, and dependency scanning

It is currently structured as a **traditional Jakarta web application**:

- **Servlets** handle HTTP requests
- **JSP** renders HTML views
- **MongoDB driver** provides persistence
- **GlassFish 7** hosts the WAR

---

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Packaging | Maven WAR |
| Web layer | Jakarta Servlet 6.1 + JSP 3.1 |
| App server | Eclipse GlassFish 7 |
| Persistence | MongoDB Java Sync Driver 5.2.1 |
| Logging | Log4j2 2.25.3 |
| Testing dependency | JUnit 4.13.1 |
| Security scan | OWASP Dependency-Check Maven Plugin 12.1.0 |

---

## Current application features

### 1. Landing page

File: `src/main/webapp/index.jsp`

The home page exposes a simple starting point for the app and links to the assets page:

- `/cmdb/`
- link to `/cmdb/assets`

### 2. Assets module

Primary files:

- `src/main/java/com/xhait/ti/cmdb/assets/Asset.java`
- `src/main/java/com/xhait/ti/cmdb/assets/AssetsDao.java`
- `src/main/java/com/xhait/ti/cmdb/web/AssetsServlet.java`
- `src/main/webapp/WEB-INF/jsp/assets.jsp`

What it does:

- `GET /assets` lists the latest assets from MongoDB
- `POST /assets` creates a new asset and redirects back to the list
- data is stored in the MongoDB collection **`Assets`**

Document shape used by the app:

```json
{
  "_id": "ObjectId",
  "name": "String",
  "type": "String | null",
  "owner": "String | null",
  "createdAt": "Date"
}
```

Behavior from the code:

- `name` is required
- `type` and `owner` are optional
- `createdAt` is generated automatically on insert
- list results are sorted by `createdAt` descending
- list size is capped between `1` and `200`, with the servlet currently requesting `50`

### 3. Health endpoint

File: `src/main/java/com/xhait/ti/cmdb/web/HealthServlet.java`

Route:

- `GET /health`

Response format:

```text
OK
contextPath=/cmdb
servletPath=/health
```

This is the fastest way to confirm the app is deployed and serving requests.

### 4. Logging bootstrap

Files:

- `src/main/resources/log4j2.xml`
- `src/main/java/com/xhait/ti/cmdb/web/LogDirInitializer.java`

What happens:

- on startup, the app tries to create `D:\log_apps`
- Log4j2 writes to:
  - `D:/log_apps/cmbd.log`
  - rolled files like `D:/log_apps/cmbd-YYYY-MM-DD-i.log.gz`
- logs also go to the console

> Note: the file name is currently `cmbd.log` in the config, not `cmdb.log`.

### 5. Dependency report launcher utility

File:

- `src/main/java/com/xhait/ti/cmdb/DependencyReportLauncher.java`

This is a build-time utility used by the Maven `launch-dependency-report` profile. It attempts to open the generated OWASP report in a browser on supported local environments.

---

## Project structure

```text
cmdb/
├── pom.xml
├── README.md
├── LICENSE
├── src/
│   ├── main/
│   │   ├── java/com/xhait/ti/cmdb/
│   │   │   ├── DependencyReportLauncher.java
│   │   │   ├── assets/
│   │   │   │   ├── Asset.java
│   │   │   │   └── AssetsDao.java
│   │   │   ├── mongo/
│   │   │   │   └── MongoClientProvider.java
│   │   │   └── web/
│   │   │       ├── AssetsServlet.java
│   │   │       ├── HealthServlet.java
│   │   │       └── LogDirInitializer.java
│   │   ├── resources/
│   │   │   └── log4j2.xml
│   │   └── webapp/
│   │       ├── index.jsp
│   │       ├── META-INF/
│   │       │   └── MANIFEST.MF
│   │       └── WEB-INF/
│   │           ├── jsp/
│   │           │   └── assets.jsp
│   │           └── web.xml
│   └── test/
│       ├── java/
│       └── resources/
└── target/
    ├── cmdb.war
    ├── cmdb/
    └── dependency-check-report/
```

---

## Runtime architecture

### Request flow

For the assets feature, the request path is:

1. browser requests `/cmdb/assets`
2. `AssetsServlet` handles the request
3. `AssetsDao` talks to MongoDB through `MongoClientProvider`
4. servlet places `assets` on the request
5. `/WEB-INF/jsp/assets.jsp` renders the HTML table

### MongoDB configuration resolution

`MongoClientProvider` reads configuration in this order:

1. environment variables
   - `MONGODB_URI`
   - `MONGODB_DB`
2. Java system properties
   - `mongodb.uri`
   - `mongodb.db`

If the values are missing, the app throws an `IllegalStateException`, and `AssetsServlet` returns a friendly HTML error page explaining the missing configuration.

### Servlet mappings

From `src/main/webapp/WEB-INF/web.xml`:

| Route | Servlet | Purpose |
|---|---|---|
| `/assets` | `AssetsServlet` | list and create assets |
| `/health` | `HealthServlet` | deployment smoke test |

---

## Prerequisites

Verified from the project and workspace:

- **JDK 17**
- **Apache Maven**
- **Eclipse GlassFish 7**
- **MongoDB**, if you want the assets page to work end-to-end

Optional:

- Eclipse IDE for import/publish workflow
- NVD API key for faster OWASP Dependency-Check runs

---

## Build

From the project root:

```cmd
cd /d D:\eclipse-workspace\cmdb
mvn clean package
```

Expected artifact:

- `target\cmdb.war`

### Build notes

- packaging is `war`
- final artifact name is `cmdb.war`
- the current repo has no committed unit/integration test classes under `src/test/java`
- JUnit is present as a dependency, but no active test suite is currently checked in

---

## Deploy

### Option 1 — Maven autodeploy profile

The `pom.xml` contains a Windows GlassFish autodeploy path:

- `D:\glassfish7\glassfish\domains\domain1\autodeploy`

Use:

```cmd
cd /d D:\eclipse-workspace\cmdb
mvn clean package -Pautodeploy
```

### Option 2 — manual copy

```cmd
cd /d D:\eclipse-workspace\cmdb
copy /Y target\cmdb.war D:\glassfish7\glassfish\domains\domain1\autodeploy\cmdb.war
```

### Option 3 — Eclipse publish

1. Import `cmdb` as an existing Maven project.
2. Add it to your GlassFish server.
3. Publish the server.

---

## Verified local URLs

I verified the following endpoints in the current workspace/runtime:

| URL | Result |
|---|---|
| `http://localhost:8080/cmdb/` | `200 OK` |
| `http://localhost:8080/cmdb/health` | `200 OK` |
| `http://localhost:8080/cmdb/assets` | currently `404 Not Found` on the deployed runtime |

### Important runtime note

The **source code and built WAR do include** the `/assets` servlet mapping and classes.

That means the currently deployed GlassFish application appears to be **out of sync** with the current local source/build, or the deployed runtime is serving an older/incomplete version of the app.

If you want the deployed app to match the current repository, rebuild and redeploy `cmdb.war` before validating `/assets`.

---

## Configure MongoDB for the assets feature

Set these values before deploying or starting GlassFish in a way the server process can read them.

### Environment variables

```cmd
set MONGODB_URI=mongodb://localhost:27017
set MONGODB_DB=cmdb
```

### Alternative: JVM system properties

The code also supports:

- `-Dmongodb.uri=...`
- `-Dmongodb.db=...`

### What happens if MongoDB is not configured

`AssetsServlet` catches runtime configuration errors and returns an HTML page explaining that these values are required:

- `MONGODB_URI`
- `MONGODB_DB`

---

## Use the app

### Landing page

Open:

```text
http://localhost:8080/cmdb/
```

### Health check

Open:

```text
http://localhost:8080/cmdb/health
```

Expected response:

```text
OK
contextPath=/cmdb
servletPath=/health
```

### Assets page

Open:

```text
http://localhost:8080/cmdb/assets
```

Expected behavior when the current source is deployed and MongoDB is configured:

- displays a create form
- allows creating an asset with `name`, `type`, `owner`
- shows the latest assets in a table
- shows “No assets found.” when the collection is empty

### Create an asset from the browser

1. go to `/cmdb/assets`
2. fill in **Name**
3. optionally fill in **Type** and **Owner**
4. click **Add**
5. the servlet inserts the document and redirects back to `/assets`

---

## Maven profiles

The `pom.xml` defines these profiles:

| Profile | Purpose |
|---|---|
| `autodeploy` | copies `target\cmdb.war` into the GlassFish autodeploy directory |
| `autoclean` | deletes most of the `target` directory during Maven clean |
| `dependency-check` | runs OWASP Dependency-Check during `verify` |
| `launch-dependency-report` | launches the generated HTML report on local machines when appropriate |

### Dependency-Check usage

Run a scan:

```cmd
cd /d D:\eclipse-workspace\cmdb
mvn -Pdependency-check verify
```

Run a scan and try to open the HTML report afterward:

```cmd
cd /d D:\eclipse-workspace\cmdb
mvn -Pdependency-check,launch-dependency-report -Dlaunch.dependency-report.skip=false verify
```

Open an already generated report without running the scan again:

```cmd
cd /d D:\eclipse-workspace\cmdb
mvn -Plaunch-dependency-report -Ddependency-check.skip=true -Dlaunch.dependency-report.skip=false verify
```

### Dependency-Check environment

The scan reads:

- `NVD_API_KEY`
- optionally `OSS_INDEX_USERNAME`
- optionally `OSS_INDEX_PASSWORD`

Generated reports are written under:

- `target\dependency-check-report\dependency-check-report.html`
- `target\dependency-check-report\dependency-check-report.json`

---

## Logging

Log4j2 is configured in `src/main/resources/log4j2.xml`.

Current behavior:

- application package `com.xhait.ti.cmdb` logs at `debug`
- MongoDB loggers are set to `info`
- root logger logs at `info`
- output goes to both console and rolling file

Configured log directory:

```text
D:\log_apps
```

Configured main log file:

```text
D:\log_apps\cmbd.log
```

---

## Known gaps and caveats

Based on the current repo contents and live checks:

1. **No test classes are currently checked in** under `src/test/java`.
2. **The deployed `/assets` route currently returns 404**, even though the source and built WAR include it.
3. **MongoDB is required** for the assets feature to operate normally.
4. **Logging path is hardcoded** to `D:\log_apps`.
5. The app is currently a **starter/demo CMDB**, not yet a full enterprise CMDB with authentication, editing, deletion UI, search, relationships, or inventory workflows.

---

## Troubleshooting

| Problem | What to check |
|---|---|
| `http://localhost:8080/cmdb/` does not open | confirm GlassFish is running and `cmdb.war` is deployed |
| `/health` fails | republish/redeploy the WAR and verify the context root is `/cmdb` |
| `/assets` returns 404 | the deployed server likely does not match the current source; rebuild and redeploy `target\cmdb.war` |
| `/assets` returns 500 with Mongo config message | set `MONGODB_URI` and `MONGODB_DB` for the GlassFish process |
| asset creation fails | ensure `name` is not blank; `AssetsDao.insert()` rejects blank names |
| file logs are missing | confirm `D:\log_apps` is writable by the server process |
| dependency check is slow or rate-limited | set `NVD_API_KEY` in the environment |

---

## Quick start

### 1. Build

```cmd
cd /d D:\eclipse-workspace\cmdb
mvn clean package
```

### 2. Deploy

```cmd
copy /Y D:\eclipse-workspace\cmdb\target\cmdb.war D:\glassfish7\glassfish\domains\domain1\autodeploy\cmdb.war
```

### 3. Verify

```text
http://localhost:8080/cmdb/
http://localhost:8080/cmdb/health
http://localhost:8080/cmdb/assets
```

### 4. If `/assets` should work

Make sure the deployed GlassFish process has MongoDB configuration:

```cmd
set MONGODB_URI=mongodb://localhost:27017
set MONGODB_DB=cmdb
```

---

## File reference

| File | Purpose |
|---|---|
| `pom.xml` | WAR packaging, dependencies, Maven profiles |
| `src/main/webapp/index.jsp` | landing page |
| `src/main/webapp/WEB-INF/web.xml` | servlet mappings |
| `src/main/webapp/WEB-INF/jsp/assets.jsp` | assets HTML view |
| `src/main/java/com/xhait/ti/cmdb/assets/Asset.java` | asset model |
| `src/main/java/com/xhait/ti/cmdb/assets/AssetsDao.java` | MongoDB persistence for assets |
| `src/main/java/com/xhait/ti/cmdb/mongo/MongoClientProvider.java` | Mongo client/database configuration |
| `src/main/java/com/xhait/ti/cmdb/web/AssetsServlet.java` | assets list/create web endpoint |
| `src/main/java/com/xhait/ti/cmdb/web/HealthServlet.java` | health endpoint |
| `src/main/java/com/xhait/ti/cmdb/web/LogDirInitializer.java` | startup log directory creation |
| `src/main/resources/log4j2.xml` | logging configuration |
| `src/main/java/com/xhait/ti/cmdb/DependencyReportLauncher.java` | launches dependency scan report |

---

## Summary

`cmdb` is currently a **Mongo-backed Jakarta webapp starter** with a simple assets workflow, a health endpoint, Windows-oriented GlassFish deployment defaults, and a built-in OWASP dependency scanning workflow.

The repository already contains the code for the assets feature; the main thing to verify next in runtime is that GlassFish is serving the freshly built WAR and has MongoDB configuration available to the app process.