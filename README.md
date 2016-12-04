# VSJWS
VSJWS is a very simple java web server. It can do many things:
* Supports GET, POST, PUT, DELETE, HEAD and PATCH methods
* Can easily respond a file
* Simple logging
* Filters supporting
* Provides cookies and sessions
* Can be embedded in existing project
* Runnable as a '.jar' program

But also it has things that are not done yet:
* No file uploading support
* No HTTPS support (but can be implemented using NGINX)


## Code structure

The main class is [WebServer](https://github.com/babobka/VSJWS/tree/master/src/main/java/ru/babobka/vsjws/webserver) . It has a run() method to run a server. There is a loop inside that waits for a socket to handle. Then, the socket goes to [SocketProcessorRunnable](https://github.com/babobka/VSJWS/blob/master/src/main/java/ru/babobka/vsjws/runnable/SocketProcessorRunnable.java).
SocketProcessorRunnable determines what web controller must process a given request using its URL and HTTP method.

## Code examples

### [Sample project](https://github.com/babobka/VSJWSSamples) 

### How to run a server

```java

	private static final int PORT = 2512;

	private static final int SESSION_TIMEOUT_SECS = 15 * 60;

	private static final String SERVER_NAME = "Sample server";

	private static final String LOG_FOLDER = "server_log";

	public static void main(String[] args) throws IOException {

		WebServer webServer = new WebServer(SERVER_NAME, PORT, SESSION_TIMEOUT_SECS, LOG_FOLDER);
		// Adding controllers for a specified URLs
		webServer.addController("json", new JsonTestController());
		webServer.addController("xml", new XmlTestController());
		webServer.addController("heavy", new HeavyRequestController());
		webServer.addController("error", new InternalErrorController());
		webServer.addController("session", new SessionTestController());
		webServer.addController("simpleForm", new SimpleFormController());
		webServer.addController("xslt", new XsltTestController());
		webServer.addController("cookies", new CookieTestController());
		webServer.addController("redirect", new RedirectTestController());
		webServer.addController("", new MainPageController());
		webServer.start
	}

```

### How to code a web controller

```java

public class MainPageController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException  {
		return HttpResponse.resourceResponse("web-content/main.html");

	}
}
```

### How to code a web filter
```java

public class AuthWebFilter implements WebFilter {

	private static final String LOGIN = "user";

	private static final String PASSWORD = "123";


	@Override
	public void afterFilter(HttpRequest request, HttpResponse response) {		
		//Do nothing
	}

	@Override
	public HttpResponse onFilter(HttpRequest request) {
		String login = request.getHeader("X-Login");
		String password = request.getHeader("X-Password");		
		if (!login.equals(LOGIN) || !password.equals(PASSWORD)) {
			// Show error response
			return HttpResponse.textResponse("Bad login/password combination",
					ResponseCode.UNAUTHORIZED);
		} else {
			//Do nothing. Proceed.
			return null;
		}
	}

}
```

In order to run a web filter, you need to add it to a given web controller:
```java
webServer.addController("/", new MainPageController().addWebFilter(new AuthWebFilter()));
```
There may be more filters. You can easily add a new one like this:

```java
webServer.addController("/", new MainPageController().
								addWebFilter(new AuthWebFilter()).
										addWebFilter(new AnotherWebFilter()));
```
Filters will be executed one by one in a queue style.
