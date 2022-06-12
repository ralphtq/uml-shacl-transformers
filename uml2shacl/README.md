# EDG app for importing files from Rhapsody

## Prerequisites

* JDK 11+
* sbt 1.0+

## Building and running the app

    script/server

## Installing the app into EDG

Once the server is running

    script/install

to install the app into EDG. The script assumes the app is run with `script/server` and EDG is available
at `http://localhost:8080/`.

## Development

### Structure of this repository

* `doc/MBSE Instance Data.pdf`: OneNote from Tim about algorithms for converting .clsx files to the target instance RDF;
  implemented in the one-off SPARQL in `doc/sparql`
* `doc/sparql`: one-off SPARQL queries created by Tim for converting TopBraid Composer Semantic XML (RDF) derived from
  .clsx files to the target instance RDF; each `.sparql` file corresponds to a top-level XML element type
* `module/lib`: library used by the command line interface (CLI) and the app
* `module/lib/src/gen/scala`: case classes generated from the `Union.xsd` using `scalaxb`
* `module/lib/src/main/scala`: importer web application; see package object and class inline documentation
* `module/lib/src/main/xsd`: `Union.xsd` file, generated from `Union.clsx` by IntelliJ
* `module/lib/src/main/webapp`: `web.xml` for the importer web application, bundled in the .war
* `module/lib/src/test/resources/clsx`: example Rhapsody XML (.clsx) files sent by Tim; they do not describe a real
  system
* `module/lib/src/test/resources/clsx/Union.clsx`: union of the individual .clsx files, generated
  by `script/concatenate-clsx.py`
* `module/lib/src/test/resources/ttl`: example instance RDF data created by Tim's SPARQL queries from the .clsx files;
  used as a check on the output of the transformation process
* `module/lib/src/test/scala`: unit tests for the importer web application

### Transformation process

The transformation process works as follows:

Development time:

1. The `Union.xsd` XML schema is generated from instance data (.clsx XML files) manually using a tool such as XML Spy or
   IntelliJ
1. `script/generate-xsd-scala` generates Scala types from the `Union.xsd` using `scalaxb`

Runtime:

1. The `XmlToRdfTransformer` reads XML from a string and uses the `scalaxb`-generated code to convert the XML tree to a
   tree of Scala case classes (`Xsd`*) that exactly mirror the XML structure
1. The code in `transformer.xml` transforms the `Xsd`* case classes to an abstract syntax tree ("models" in `.model`) of
   case classes.
1. The code in `.transformer.rdf` transforms the abstract syntax tree to RDF

#### Adapting to XML schema changes

Following the above:

1. Generate a new `Union.xsd`
1. Invoke `script/generate-xsd-scala` to generate new Scala types
1. Adapt the code in `transformer.xml` to read the generated Scala case classes into the same abstract syntax tree (
   models)
1. If necessary, adapt the abstract syntax tree and the RDF output
