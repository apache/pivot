//
// README for Pivot859
//

This test must be run from a real browser, so before running the test you must generate Pivot jars, with the usual ant commands:

ant package-tests
ant deploy

Note that this is required because in this test, to give grants to make queries outside the Plugin sandbox,
we must use the signed version of jars.

Then copy the html page from this package to a temporary folder (for example one folder up, relative to generated jar files),
edit it to set the Pivot Version and the relative path of jars to load,
and finally open the html page here in a real browser.

Note that this test put multiple Applet instance inside the same HTML page,
and any Applet instance does an HTTP Query via GET.
