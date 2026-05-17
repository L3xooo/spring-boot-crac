## Spring Boot, JVM and CRaC

This is a sample project to demonstrate Spring Boot 3.2+ CRaC support by creating a checkpoint and then restoring your application using it.

### Step 1: Prerequisites
* Download JDK 17 with CRaC support provided by Azul From [here](https://www.azul.com/downloads/?version=java-17-lts&os=ubuntu&architecture=x86-64-bit&package=jdk-crac#zulu
) and install it. This one is for Debian based OS like Ubuntu.
```
sudo dpkg -i zulu17.58.25-ca-crac-jdk17.0.15-linux_amd64.deb
```
* Grant appropriate permissions for CRIU to be functional. Note that you need to be present in your java home directory of the JDK you downloaded in previous step.
```
sudo chown root:root ./lib/criu

sudo chmod u+s ./lib/criu
```
### Step 2: Build
```
mvn clean package
```

### Step 3: Deploy
```
/usr/lib/jvm/zulu-17-crac-amd64/bin/java -XX:CRaCCheckpointTo=./tmp_chkpoint -jar ./target/crac-demo-0.0.1-SNAPSHOT.jar
```

### Step 4: Checkpoint
Find the PID of the running instance and use it to create an application checkpoint:
```
jcmd 189254 JDK.checkpoint
```

### Step 5: Restore
Restore and bring your instance back up:
```
/usr/lib/jvm/zulu-17-crac-amd64/bin/java -XX:CRaCRestoreFrom=./tmp_chkpoint
```

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
docker run --rm -v crac-checkpoint:/checkpoint -e MODE=checkpoint-only crac-demo
```

If you want a longer warmup before checkpointing, set:
```
docker run --rm -v crac-checkpoint:/checkpoint -e MODE=checkpoint-only -e STARTUP_WAIT_SECONDS=30 crac-demo
```

#### Scenario 3: Restore from checkpoint
Resume the app from a previously saved checkpoint:
```
docker run --rm -p 8080:8080 -v crac-checkpoint:/checkpoint -e MODE=restore crac-demo
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

