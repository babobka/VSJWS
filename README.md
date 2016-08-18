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
* No redirects
* No file uploading support
* No HTTPS support (but can be implemented using NGINX)

## Code examples
### How to run a server

```java
	private static final int PORT = 2512;

	private static final int SESSION_TIMEOUT_SECS = 15 * 60;

	public static final String WEB_CONTENT_FOLDER = "web-content";

	public static void main(String[] args) throws IOException {
		WebServer webServer = new WebServer("rest server", PORT, false,
				SESSION_TIMEOUT_SECS, WEB_CONTENT_FOLDER, "rest_log", null);	
		//Adding controllers for a specified URLs			
		webServer.addController("/json", new JsonTestController());
		webServer.addController("/xml", new XmlTestController());
		webServer.addController("/encoding", new EncodingTestController());
		webServer.addController("/heavy", new HeavyRequestController());
		webServer.addController("/error", new InternalErrorController());
		webServer.addController("/session", new SessionTestController());
		webServer.addController("/simpleForm", new SimpleFormController());
		webServer.addController("/xslt", new XsltTestController());
		webServer.addController("/cookies", new CookieTestController());
		webServer.addController("/", new MainPageController());
		webServer.run();
	}
```

### How to code a web controller

```java
public class MainPageController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException {
		return HttpResponse.fileResponse(new File("web-content/main.html"));

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
		if ((login == null || password == null)
				|| (!login.equals(LOGIN) || !password.equals(PASSWORD))) {
			// Show error response
			return HttpResponse.textResponse("Bad login/password combination",
					ResponseCode.UNAUTHORIZED,
					ContentType.PLAIN);
		} else {
			//Do nothing. Proceed.
			return null;
		}
	}

}
```

In order to run a web filter, you need to add it to given web controller:
```java
webServer.addController("/", new MainPageController().addWebFilter(new AuthWebFilter()));
```
There may be more filters. You can easily add a new one like this:

```java
webServer.addController("/", new MainPageController().addWebFilter(new AuthWebFilter()).addWebFilter(new AnotherWebFilter()));
```
Filters will be executed one by one in a queue style.
