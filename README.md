# CRaC Demo

Build the image:
```
docker build -t crac-demo .
```

Spin up the docker compose file:
```
docker compose up -d
```

## Scenario 1: Run the application without CRaC
```
docker run --rm -p 8080:8080 crac-demo
```

## Scenario 2: Create checkpoint only and restore
Start the app, let it warm up, then create a checkpoint and exit:
If needed to provide the `--network` flag to ensure the container can access the database, if databases are not in different network
remove the `--network some-network` flag if not needed:

```
docker run --rm --network some-network -v crac-checkpoint:/checkpoint -e MODE=checkpoint-only -e SPRING_PROFILES_ACTIVE=default,docker crac-demo
```
Resume the app from a previously saved checkpoint:
```
docker run --rm --network some-network -p 8080:8080 -v crac-checkpoint:/checkpoint -e MODE=restore -e SPRING_PROFILES_ACTIVE=default,docker crac-demo
```

Test the application by sending a request to the endpoint:
```
curl http://localhost:8080/hello
curl http://localhost:8080/mongo/save
curl http://localhost:8080/postgres/save
```