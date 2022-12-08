# solid-memory!  
(random name github suggested when I created the repo)

Minimalistic PDF file share service. Use `/public/share` to upload a PDF and set the timeout after which the file isn't accessible anymore.

Build it with `mvn package`, set env like 
```bash
export FILE_STORE_DIR=. # set a writable directory
export PORT=9000 #optional, defaults to 8080   
```
and run it with `java -jar target/solid-memory*.jar`
