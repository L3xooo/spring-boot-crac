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
docker run --rm --network tangram_default -v crac-checkpoint:/checkpoint -e MODE=checkpoint-only -e SPRING_PROFILES_ACTIVE=default,docker crac-demo
```

#### Scenario 3: Restore from checkpoint
Resume the app from a previously saved checkpoint:
```
docker run --rm --network tangram_default -p 8080:8080 -v crac-checkpoint:/checkpoint -e MODE=restore -e SPRING_PROFILES_ACTIVE=default,docker crac-demo
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