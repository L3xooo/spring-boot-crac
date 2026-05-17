### Docker:3 modes for checkpoint and restore

Build the image:
```
docker build -t crac-demo .
```

#### Scenario 1: Normal run (no checkpoint)
Just run the app normally without using CRaC:
```
docker run --rm -p 8080:8080 crac-demo
```

#### Scenario 2: Create checkpoint only
Start the app, let it warm up, then create a checkpoint and exit:
```
docker run --rm -v crac-checkpoint:/checkpoint -e MODE=checkpoint-only -e SPRING_PROFILES_ACTIVE=docker crac-demo
```

#### Scenario 3: Restore from checkpoint
Resume the app from a previously saved checkpoint:
```
docker run --rm -p 8080:8080 -v crac-checkpoint:/checkpoint -e MODE=restore -e SPRING_PROFILES_ACTIVE=docker crac-demo
```

This will start the app in seconds using the previously saved state.

### Experimental: bake checkpoint into the image during build
If you want the checkpoint stored inside the image filesystem, use the alternate Dockerfile:
```
docker build -f Dockerfile.prebaked-checkpoint -t crac-demo-prebaked .
```

Then run the image normally. It will restore from the baked checkpoint by default:
```
docker run --rm -p 8080:8080 crac-demo-prebaked
```

Notes:
- This is experimental and less portable than the runtime checkpoint volume flow.
- The checkpoint is created during `docker build`, so build and runtime environments must be compatible.
- If the build-time checkpoint fails, the image build will fail too.

### MongoDB support
MongoDB is enabled only when you activate the `mongo` profile.

Run with MongoDB available:
```
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=mongo \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/cracdemo \
  crac-demo
```

Save a document through the Mongo repository:
```
curl -X POST http://localhost:8080/mongo/save \
  -H 'Content-Type: application/json' \
  -d '{"message":"hello from mongo"}'
```

This setup keeps prebake checkpoint creation safe even when MongoDB is down, because the default profile does not require MongoDB.

