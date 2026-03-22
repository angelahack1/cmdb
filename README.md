# CMDB – Configuration Management Database

**XAIHT Tlamatini Infrastructure Demo**

Jakarta EE 10 web application on GlassFish 7 with MongoDB 7.0, fully automated with Ansible provisioning and Jenkins CI/CD.

---

## Repository Structure

```
cmdb-infra/
├── pom.xml                          # Maven build (Java 17, Jakarta EE 10)
├── .gitignore
│
├── src/main/
│   ├── java/com/xhait/ti/cmdb/
│   │   ├── DependencyReportLauncher.java
│   │   ├── assets/
│   │   │   ├── Asset.java           # POJO for asset documents
│   │   │   └── AssetsDao.java       # MongoDB DAO layer
│   │   ├── mongo/
│   │   │   └── MongoClientProvider.java  # Singleton MongoClient
│   │   └── web/
│   │       ├── AssetsServlet.java    # GET/POST /assets
│   │       ├── HealthServlet.java    # GET /health
│   │       └── LogDirInitializer.java
│   ├── resources/
│   │   └── log4j2.xml               # Log4j2 configuration
│   └── webapp/
│       ├── index.jsp
│       └── WEB-INF/
│           ├── web.xml              # Jakarta Servlet 6.0 descriptor
│           └── jsp/
│               └── assets.jsp       # Asset list + create form
│
├── mongo/
│   └── bootstrap-cmdb.js           # mongosh seed script
│
├── ansible/
│   ├── ansible.cfg                  # Ansible settings
│   ├── requirements.yml             # Galaxy collections
│   ├── vault/
│   │   └── vault_secrets.yml        # Encrypted secrets
│   ├── inventories/
│   │   ├── dev/
│   │   │   ├── hosts.yml
│   │   │   └── group_vars/all.yml
│   │   ├── staging/
│   │   │   ├── hosts.yml
│   │   │   └── group_vars/all.yml
│   │   └── prod/
│   │       ├── hosts.yml
│   │       └── group_vars/all.yml
│   ├── playbooks/
│   │   ├── site.yml                 # Master playbook (runs all)
│   │   ├── infra.yml                # Common + Java on all hosts
│   │   ├── database.yml             # MongoDB provisioning
│   │   ├── appserver.yml            # GlassFish provisioning
│   │   ├── deploy.yml               # WAR deployment
│   │   ├── ci.yml                   # Jenkins server setup
│   │   └── rollback.yml             # Rollback to previous WAR
│   └── roles/
│       ├── common/                  # OS hardening, firewall, NTP
│       ├── java/                    # OpenJDK 17 installation
│       ├── glassfish/               # GlassFish 7 + systemd + JVM config
│       ├── mongodb/                 # MongoDB 7 + auth + seed data
│       ├── deploy/                  # WAR deploy with backup + health check
│       └── jenkins_server/          # Jenkins LTS + Maven + Ansible + plugins
│
└── jenkins/
    ├── Jenkinsfile                  # Declarative CI/CD pipeline
    └── seed-job.groovy              # Job DSL auto-creation script
```

---

## Architecture Overview

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Jenkins    │────▶│  GlassFish  │────▶│  MongoDB    │
│   CI/CD      │     │  7  (App)   │     │  7.0 (DB)   │
│              │     │             │     │             │
│  • Build     │     │  • cmdb.war │     │  • cmdb DB  │
│  • Test      │     │  • JVM opts │     │  • Assets   │
│  • OWASP     │     │  • systemd  │     │  • Auth     │
│  • Deploy    │     │             │     │             │
└─────────────┘     └─────────────┘     └─────────────┘
   (ci.yml)          (appserver.yml)     (database.yml)
        │                   ▲                   ▲
        │                   │                   │
        └───── Ansible deploy.yml ──────────────┘
```

---

## Quick Start

### Prerequisites

| Tool       | Version   | Purpose                  |
|------------|-----------|--------------------------|
| Ansible    | ≥ 2.15    | Infrastructure automation|
| Java       | 17        | Application runtime      |
| Maven      | ≥ 3.9     | Build toolchain          |
| Jenkins    | LTS       | CI/CD server             |
| MongoDB    | 7.0       | Document database        |
| GlassFish  | 7.x       | Jakarta EE app server    |

### Demo MongoDB Application Credentials

The application database is `cmdb` and the demo application login is:

- Username: `cmdbApp`
- Password: `changeme`

If you run the WAR locally without extra JVM properties, the app now falls back to this local connection profile:

- Host: `localhost`
- Port: `27017`
- Database: `cmdb`
- URI: `mongodb://cmdbApp:changeme@localhost:27017/cmdb?authSource=cmdb`

### 1. Install Ansible Galaxy Collections

```bash
cd ansible/
ansible-galaxy collection install -r requirements.yml
```

### 2. Encrypt the Vault

```bash
# Edit the secrets first:
vim ansible/vault/vault_secrets.yml

# Then encrypt:
ansible-vault encrypt ansible/vault/vault_secrets.yml
```

### 3. Update Inventory IPs

Edit `ansible/inventories/dev/hosts.yml` (and staging/prod) with your actual server IPs.

### 4. Provision the Full Stack

```bash
cd ansible/
ansible-playbook -i inventories/dev/hosts.yml playbooks/site.yml \
    --vault-password-file ~/.ansible/vault_pass_cmdb
```

### 5. Build and Deploy Manually

```bash
# Build
mvn clean package -B

# Optional: seed a local MongoDB instance with the demo app credentials
mongosh "mongodb://cmdbApp:changeme@localhost:27017/cmdb?authSource=cmdb" --file mongo/bootstrap-cmdb.js

# Deploy via Ansible
cd ansible/
ansible-playbook -i inventories/dev/hosts.yml playbooks/deploy.yml \
    -e "app_war_source=../target/cmdb.war" \
    --vault-password-file ~/.ansible/vault_pass_cmdb
```

### 6. Rollback

```bash
cd ansible/
ansible-playbook -i inventories/dev/hosts.yml playbooks/rollback.yml \
    --vault-password-file ~/.ansible/vault_pass_cmdb
```

---

## Ansible Roles Reference

### `common`
Applies to **all hosts**. Installs baseline packages, enables firewalld with per-environment ports, configures chronyd (NTP), disables root SSH, enforces key-only auth, sets SELinux enforcing, creates a `deploy` user, and sets timezone to `America/Mexico_City`.

### `java`
Installs OpenJDK 17 via the system package manager and sets `JAVA_HOME` globally via `/etc/profile.d/java.sh`.

### `glassfish`
Downloads and extracts GlassFish 7, creates a `glassfish` system user, installs a `systemd` service unit, creates the admin password file, starts the domain, and configures JVM system properties for the MongoDB connection (`mongodb.uri`, `mongodb.db`) plus `log.dir`.

### `mongodb`
Adds the official MongoDB 7.0 YUM repository, installs `mongodb-org` and `mongosh`, deploys `mongod.conf` with authorization enabled, creates the admin user and the application-level `cmdbApp` user (default password in this demo: `changeme`), and runs the `bootstrap-cmdb.js` seed script.

### `deploy`
Handles zero-downtime WAR deployment: backs up the existing WAR, undeploys, deploys the new WAR via `asadmin`, runs a health check loop against `/health`, and cleans up temp files. Uses `serial: 1` for rolling deployment in production.

### `jenkins_server`
Installs Jenkins LTS from the official RPM repo, installs Maven, installs Ansible on the Jenkins node, configures plugins (git, pipeline, ansible, blueocean, owasp-dependency-check, etc.), and copies the Jenkinsfile for reference.

---

## Jenkins Pipeline Stages

| #  | Stage                       | Description                                            |
|----|-----------------------------|--------------------------------------------------------|
| 1  | Checkout                    | Clean workspace, checkout SCM                          |
| 2  | Build & Test                | `mvn clean package` with unit tests                    |
| 3  | OWASP Dependency-Check      | Scans dependencies for CVEs (skip via parameter)       |
| 4  | Archive                     | Archives WAR + fingerprints; stashes for downstream     |
| 5  | Provision Infrastructure    | Optional full Ansible `site.yml` run                   |
| 6  | Deploy to DEV               | Ansible `deploy.yml` against dev inventory             |
| 7  | Smoke Test                  | cURL health endpoint to verify deployment              |
| 8  | Deploy to STAGING           | Manual approval gate → Ansible deploy to staging       |
| 9  | Deploy to PROD              | Manual approval (restricted submitters) → rolling deploy|

### Jenkins Setup

1. **Credentials required** (Manage Jenkins → Credentials):
   - `ansible-vault-password` — Secret file containing the vault password
   - `github-credentials` — Username/password or SSH key for Git

2. **Tools to configure** (Global Tool Configuration):
   - JDK 17 → `JAVA_HOME = /usr/lib/jvm/java-17-openjdk`
   - Maven → `MAVEN_HOME = /opt/maven/current`

3. **Create the job automatically** using the seed script:
   - Create a Freestyle job named `seed-cmdb`
   - Add a "Process Job DSLs" build step
   - Point to `jenkins/seed-job.groovy`
   - Run it → the `cmdb-pipeline` job is created automatically

---

## Environment Strategy

| Environment | Inventory               | MongoDB bind  | Deploy gate  |
|-------------|-------------------------|---------------|--------------|
| dev         | `inventories/dev/`      | 0.0.0.0       | Automatic    |
| staging     | `inventories/staging/`  | 0.0.0.0       | Manual       |
| prod        | `inventories/prod/`     | 127.0.0.1     | Manual + ACL |

---

## Security Considerations

- **Vault**: All passwords stored in `ansible/vault/vault_secrets.yml`, encrypted with `ansible-vault`.
- **SSH**: Root login disabled, password auth disabled, key-only access.
- **SELinux**: Enforcing mode on all RHEL hosts.
- **Firewall**: Only required ports opened per environment.
- **MongoDB**: Authorization enabled; separate admin and app users.
- **GlassFish**: Admin password secured via `.pwdfile` (mode 0600).
- **OWASP**: Dependency-Check integrated into both Maven and Jenkins pipeline.
- **Jenkins**: Prod deploys restricted to `admin` and `ops-team` submitters.
- **Jakarta EE**: Secure session cookies (HttpOnly + Secure flags in web.xml).

---

## Useful Commands

```bash
# Syntax check a playbook
ansible-playbook playbooks/site.yml --syntax-check

# Dry-run (check mode)
ansible-playbook -i inventories/dev/hosts.yml playbooks/deploy.yml \
    -e "app_war_source=../target/cmdb.war" --check --diff

# Run only a specific role by tag
ansible-playbook -i inventories/dev/hosts.yml playbooks/site.yml \
    --tags "mongodb"

# View vault secrets (prompts for password)
ansible-vault view ansible/vault/vault_secrets.yml

# Re-encrypt vault with a new password
ansible-vault rekey ansible/vault/vault_secrets.yml
```

---

## License

Proprietary — XAIHT © 2026