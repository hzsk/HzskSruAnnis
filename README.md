# HZSKSRU–Hamburger Zentrum für Sprachkorpora Search/Retrieve via URI

This is a java implementation of SRU for HZSK with CLARIN-FCS stuffs included.

## Dependencies

Ideally maven commands will automagicate these:

* Java
* Maven
* Tomcat
* JDBC
* CQL java
* Exmaralda 1.9 (this may not be automagic?)

From CLARIN.eu repo:

* SRUServer
* FCSSimpleEndPoint
* (and their deps)

## Usage

I currently work with it by doing things like:
```
mvn compile
mvn package
mvn install
cp target/*.jar $MAVENDEPLOY/
```

I guess you could also set up the mvn deploy target somehow.

You need to query the web address with parameters operation, version and query.

## Servlet stuff and settings

The connection to mySQL database holding dumped exmaralda corpora is in
`src/main/webapp/META-INF/context.xml`, this is not distributed. The XML's in
`src/main/webapp/WEB-INF/` have some static metadata view on the project, but
if the database works, lots of things are pulled from there. There's also some
settings.

## Code layout

There's standard API doc stuff in the code you can use, e.g. in target/apidocs/.
It's always the up-to-date place to look at things. I'll describe here briefly
and informally how this package works. There aren't that many things in it:
HZSKSRUSearchEngine is the main class, the CorpusConnection is a database
implementation of searches, DBSearch classes are simple structs for storing the
DB results, HZSKSRU Result classes wrap DB results to be compliant with FCS/SRU
stuff.

## Database accesses

I drew a graph of the subset of the DB in use for these searches in
 `dev/corpora-db.svg` (not distributed). The DB is automatically converted from
exmaralda files, the whole system has dozens of tables but lot of data is empty.

A default request or searchRetrieve is parsed as CQL or FCS search and turned
into a simple text search targetted to database. The default search only finds
text from annotated segments that are of type sc and name speakerContribution.
The advanced search atm finds all segments with parent matching this segment,
later versions should probably too more exact matching and layer selections.

A request for SRU explain *may* be resolved using a database query, but since
my test databases don't have this informations set up, we have a static
endpoint description in WEB-INF dir.

SRU scan doesn't have anything yet, either upstream classes provide something
for it or not.

## HZSK git

If you are working within HZSK please also push to $GITDIR/HZSKsru.git.
