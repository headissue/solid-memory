# solid-memory!  
(random name GitHub suggested when I created the repo)

Minimalistic PDF file share service. Navigate your browser to `/public/share` to upload a PDF and set the timeout after which the file isn't accessible anymore. PDFs are rendered when opening `/docs/${accessId}` only after the visitor entered their email address. The PDFs are rendered by PDF.js and we don't offer an easy way to download or print the file.

## pdf access rules are stored as yaml files
Example
```yaml
fileName: shared.pdf
ttlDays: 2      # optional, set 0, null or remove it to mark "does not expire"
utmParameters:  # optional, set null or remove
  source: ""    # optional, set null or remove
  medium: ""    # optional, set null or remove
  campaign: ""  # optional, set null or remove
  term: ""      # optional, set null or remove
  content: ""   # optional, set null or remove
```

## prod build and run
Build it with `mvn package`, set env like 
```bash
export FILE_STORE_DIR=/path/to/dir # set a writable directory
export PORT=9000 #optional, defaults to 8080
```
and run it with `java -jar target/solid-memory*.jar`