- [x] https/ssl raus
- [x] klassennamen aus yaml raus
- [x] ttl days
- [ ] ttl is optional, like `Optional<Long>`, that's not how we do it https://dzone.com/articles/optional-anti-patterns
- [x] ttlDays is nullable Long
- [x] ttlDays can be omitted in yaml
- [x] email eingabe sollte nicht im link sichtbar sein
- [X] utm codes for link, utm_source, utm_campaign, utm_medium, utm_term, utm_content
- [x] url without prefix and 8 chars: https://xyz.cc/abcd1234, I used NanoId default `a-zA-z0-9_-`
- [x] some static text at /index.html
- [x] footer with link to privacy bla
- [x] move Previous / Next to the right. "Page 1 / 4 Previous Next"
- [x] Owner field: Please enter your email to continue. Add "Your data will only be shared with John Doe". http://localhost:8080/00000000 
- [x] optional download button see http://localhost:8080/00000001
- [x] logging download
- [x] expired link -> please contact ${owner} to get a new one http://localhost:8080/00000002
- [x] Additional checkbox: "Send me monthly updates."
- [x] only set form action when email is entered to prevent 🤷
- [x] set and validate form key
- [x] download always allowed
- [x] bug empty mail successful, only frontend check though
- [X] preview page should always show footer
- [x] xyz.cc is a service of headissue, link imprint and contact 
- [x] auf der index groß xyz.cc und die footer links
- [x] preview: wirklich nur die erste seite ausliefern
- [x] favicon
- [x] donwload als parameter nicht url `/download`
- [x] set response header on donwload

- [x] send 404 for unknown links and don't log
- [x] generate preview page as blurry jpeg. extracting the first page from the PDF does not reduce file size much
- [x] set proper filename for download

- [ ] circle e2e test

The content should not be easily accessed by robots or search engines. Maybe the email address should be checked and a PoW implemented

## Prio C
- [x] Preview of first page and popup, after submitting the email address, the scroll function appears

Also no more warning about resubmit ;)
