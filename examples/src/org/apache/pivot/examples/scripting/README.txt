//
// README for org.apache.pivot.examples.scripting Samples
//

In this package there are some Groovy sources and even some Scala sources, but in our ant builds they are not compiled.
To run those examples from an IDE (like Eclipse), you must enable support for those languages for the project containing them (examples).

Note that in case of problems, for example a configured Nature for that project but the related Plugin is not installed,
it will not be possible to run even Java examples because nothing in that project will be compiled.
