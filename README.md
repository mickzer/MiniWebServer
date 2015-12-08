# MiniWebServer

A tiny little HTTP server I wrote in my spare time.

The server can return HTML files for GET requests and can potentially parse form data for other request methods.

To use:
* Run the main method in Main.java
* Navigate to http://localhost:5000/index.html
* Submit the form at http://localhost:5000/form.html to see a POST request

Future Plans:
* Turn the doHTTPMethod methods into a proper API that can be interfaced with by a connector of some sorts - eg.PHP connector
* Do MIME types properly
* A bunch of other stuff I haven't thought of yet 
