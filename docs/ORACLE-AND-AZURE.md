# Database and deployment notes

The original group used Oracle and Microsoft Azure. Their accounts are no longer accessible. The old Azure setup is unknown, so this project does not claim to restore it.

## Local database

H2 stores records in `data/meteor.mv.db`. The application uses Oracle compatibility mode, but this does not make H2 the same as Oracle. All current functional tests use H2.

Demo accounts are added only when the account table is empty. `DEMO_ENABLED=false` turns off that step. It does not remove existing accounts or create a new staff account.

To start again with sample data, stop the application and move the `data` folder somewhere safe. On the next startup, the application creates a new database.

## Oracle files

`application-oracle.properties` and `docs/database/oracle-schema.sql` are included for a future Oracle setup. The SQL was generated from the rebuilt Java classes. It is not the original schema and has not been run against Oracle.

Build with the optional driver using:

```powershell
.\mvnw.cmd -Poracle package
```

For that profile, set `ORACLE_JDBC_URL`, `ORACLE_USERNAME` and `ORACLE_PASSWORD` locally. Run the JAR with `--spring.profiles.active=oracle`. The profile checks an existing schema instead of creating one, so the generated SQL needs review and setup first. Demo account creation is disabled for this profile.

## Hosting

The Dockerfile is included as a starting point. It has not been run as part of this project. Nothing has been deployed to Azure. GitHub can host the source and documentation; GitHub Pages cannot run this Java backend.

Before a real public deployment, the project still needs HTTPS, private credentials, staff account setup, password recovery, migrations and backups.
