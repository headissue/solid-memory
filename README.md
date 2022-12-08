# solid-memory!  
(random name GitHub suggested when I created the repo)

Minimalistic PDF file share service. Use `/public/share` to upload a PDF and set the timeout after which the file isn't accessible anymore. PDFs are rendered when opening `/doc/${accessId}` only after the visitor entered their email address. The PDFs are rendered by PDF.js and we don't offer an easy way to download or print the file.

## prod build and run
Build it with `mvn package`, set env like 
```bash
export FILE_STORE_DIR=/path/to/dir # set a writable directory
export PORT=9000 #optional, defaults to 8080
export SSL_PORT=9443 #optional, defaults to 8443
export KEYSTORE_PATH=/path/to/keystore.jks 
export KEYSTORE_SECRET="YourSecret!" 
```
and run it with `java -jar target/solid-memory*.jar`

## dev
env:
```bash
export FILE_STORE_DIR=/path/to/dir # set a writable directory
export STAGE=local
```
