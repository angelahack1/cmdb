# cmdb

## Dependency-Check profiles

This project includes two Maven profiles for OWASP Dependency-Check:

- `dependency-check`: runs the vulnerability scan and generates reports
- `launch_dependency-report`: opens the generated HTML report in the default browser on local Windows machines

### Environment

Set the NVD API key in the `NVD_API_KEY` environment variable.

The scan profile reads it from Maven as `${env.NVD_API_KEY}`.

### Local usage

Run the scan and generate the report:

```bat
cd /d E:\eclipse-workspace\cmdb
mvn -Pdependency-check verify
```

Run the scan and open the HTML report afterward:

```bat
cd /d E:\eclipse-workspace\cmdb
mvn -Pdependency-check,launch_dependency-report -Dlaunch.dependency-report.skip=false verify
```

Open an already generated report without running the scan again:

```bat
cd /d E:\eclipse-workspace\cmdb
mvn -Plaunch_dependency-report -Ddependency-check.skip=true -Dlaunch.dependency-report.skip=false verify
```

### Jenkins-safe usage

For Jenkins, run only the scan profile:

```bat
cd /d E:\eclipse-workspace\cmdb
mvn -Pdependency-check verify
```

Notes:
- `launch_dependency-report` is guarded so it does not auto-activate when `JENKINS_URL` is present.
- Browser launch is Windows-oriented and non-fatal.
- Reports are generated under `target\dependency-check-report\`.
- The main HTML report path is `target\dependency-check-report\dependency-check-report.html`.
