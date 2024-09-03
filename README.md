## Juno Http Client
**HTTP client** for Java and Android, which facilitates the creation of **HTTP request**  such as: GET, POST, HEAD, OPTIONS, PUT, DELETE and TRACE.

## Installation

To include Juno in your project using Gradle, add the following dependency:
```
dependencies {
  implementation 'com.github.jesusbmx:juno:1.0.1'
  implementation 'com.github.jesusbmx:juno-http-client:1.0.1'
}
```

Alternatively, you can download the JAR file directly from [JitPack](https://jitpack.io/#jesusbmx/juno-http-client):

Download [juno.jar](https://jitpack.io/com/github/jesusbmx/juno/1.0.1/juno-1.0.1.jar)

Download [juno-http-client.jar](https://jitpack.io/com/github/jesusbmx/juno-http-client/1.0.1/juno-http-client-1.0.1.jar)


## [Samples](src/test/java/Samples.java)
Start by creating an instance of the `HttpClient`:
```java
HttpClient client = HttpClient.getInstance()
    .setDebug(true);
```

#### GET
```
GET https://postman-echo.com/get HTTP/1.1
```

```java
String get() throws Exception {
  HttpRequest request = new HttpRequest(
       "GET", "https://postman-echo.com/get")
  ;
  return client.execute(request, String.class);
}
```

#### POST
```
POST https://postman-echo.com/post HTTP/1.1
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
Content-Length: 25

id=7&name=bar&active=true
```

```java
String post(int id, String name, boolean active) throws Exception {
  // application-www-www-form-urlencoded
  FormBody reqBody = new FormBody()
          .add("id", id)
          .add("name", name)
          .add("active", active)
  ;
  HttpRequest request = new HttpRequest(
      "POST", "https://postman-echo.com/post", reqBody)
  ;
  return client.execute(request, String.class);
}
```

#### RequestBody
```
POST https://postman-echo.com/post HTTP/1.1
Content-Type: application/json; charset=UTF-8
Content-Length: 44

{"id": "7", "name": "bar", "active": "true"}
```

```java
String request() throws Exception {
    String json = "{\"id\": \"7\", \"name\": \"bar\", \"active\": \"true\"}";
    
    // application/json
    RequestBody reqBody = RequestBody.create(
        "application/json", json)
    ;
    HttpRequest request = new HttpRequest(
        "POST", "https://postman-echo.com/post", reqBody)
    ;
    return client.execute(request, String.class);
}
```

#### Upload
```
POST https://postman-echo.com/post HTTP/1.1
Content-Type: multipart/form-data; boundary=30707575573640
Content-Length: 71162

--30707575573640
Content-Disposition: form-data; name="name"
Content-Transfer-Encoding: 8bit
Content-Type: text/plain; charset=UTF-8

John Doe
--30707575573640
Content-Disposition: form-data; name="file"; filename="file_1710436579164.png"
Content-Transfer-Encoding: binary
Content-Type: application/octet-stream

-- binary --
```

```java
String upload(File file) throws Exception { 
  // multipart/form-data
  MultipartBody reqBody = new MultipartBody()
    .addParam("name", "John Doe")
    .addFile("file", file)
  ;
  HttpRequest request = new HttpRequest(
    "POST", "https://postman-echo.com/post", reqBody)
  ;
  return client.execute(request, String.class);
}
```

#### Download
```
GET https://jitpack.io/com/github/jesusbmx/java-http-client/0.0.1/java-http-client-0.0.1.jar HTTP/1.1
```

```java
File download() throws Exception {
  HttpRequest request = new HttpRequest("GET", "https://jitpack.io/com/github/jesusbmx/java-http-client/0.0.1/java-http-client-0.0.1.jar")
      .setTimeoutMs(20000)
  ;
  FileResponseBodyConverter converter = new FileResponseBodyConverter()
      .setDir(System.getProperty("user.home") + "\\Downloads\\") 
      //.setName("httpclient.jar")
  ;  
  return client.execute(request, converter);
  //return client.execute(request, File.class);
}
```

#### Url, Headers
```
GET http://ip-api.com/json/24.48.0.1?fields=status%2Cmessage%2Cquery%2Ccountry%2Ccity&lang=en HTTP/1.1

User-Agent: nombre-cliente
```

```java
HttpResponse getIpLocation() throws Exception { 
  HttpUrl url = new HttpUrl("http://ip-api.com/")
    .addPath("json")
    .addPath("24.48.0.1")
    .addQueryParameter("fields", "status,message,query,country,city")
    .addQueryParameter("lang", "en")
  ;
  HttpRequest request = new HttpRequest("GET", url)
    .addHeader("User-Agent", "nombre-cliente")
  ;
  return client.execute(request);
}
```
#### HttpResponse

```java
try ( HttpResponse response = getIpLocation() ) {
  int code = response.code;  
  Headers headers = response.headers;
  Charset charset = response.getCharsetFromContentType();
  InputStream content = response.content;

  String str = response.readString(); // content.close()
  byte[] bytes = response.readBytes(); // content.close()
}
```

#### Interceptor

```java
HttpClient client = HttpClient.getInstance().setInterceptor((request, stack) -> {
    HttpResponse response = stack.execute(request);
    if (response.code >= 200 && response.code <= 299) {
        return response;
    }
    throw new Exception("Unknown error code: " + response.code);
});
```



## [Asynchronous and Synchronous Tasks](src/test/java/AsynTest.java)

We prepare the request

```java
public Async<String> insert(
    int id, String name, boolean active) {
    
  // application-www-www-form-urlencoded
  FormBody reqBody = new FormBody()
      .add("id", id)
      .add("name", name)
      .add("active", active);
  
  HttpRequest request = new HttpRequest(
      "POST", "https://postman-echo.com/post", reqBody);

  return client.createAsync(request, String.class);
}
```

#### Asynchronous

Asynchronously send the request and notify your application with a callback when a response returns. Since this request is asynchronous, Restlight handles the execution in the background thread so that the
Main UI is not blocked or interferes with it.

```java
Async<String> async = insert(22, "John Doe", true);
    
async.execute((String response) -> {
  String str = response;
  System.out.println(str);

}, (Exception e) ->  {
   e.printStackTrace();
});
```

#### Synchronous

Synchronously send the request and return your response.

```java
Async<String> async = insert(22, "John Doe", true);
    
try {
    String response = async.await();
    System.out.println(response);
    
} catch(Exception e) {
    e.printStackTrace();
}
```

## [JSON](https://github.com/stleary/JSON-java)
(https://www.json.org/json-en.html)

For android it is not necessary to download [org.json jar](https://github.com/stleary/JSON-java)
For other java platforms like java swing if needed.

#### JSON response
```java
public Async<JSONObject> insert(
    String name, int age, boolean active) {
      
    // application-www-www-form-urlencoded
    FormBody reqBody = new FormBody()
        .add("name", name)
        .add("age", age)
        .add("active", active);

    HttpRequest request = new HttpRequest(
        "POST", "https://postman-echo.com/post", reqBody);
        
    return client.createAsync(request, JSONObject.class);
}
```

#### JSON request body
```java
JSONObject jsonRequest() throws Exception {
  JSONObject data = new JSONObject();
  data.put("user_id", 7);
  data.put("name", "jesus");

  RequestBody reqBody = RequestBody.create(
        "application/json", data.toString());

  HttpRequest request = new HttpRequest(
        "POST", "https://postman-echo.com/post", reqBody);

  return client.execute(request, JSONObject.class);
}
```

## Authorization

[JWT](https://jwt.io/) enables automatic storage, cleansing, transmission, and refresh of authentication tokens.

### Storage and Configuration

First, you must create your own token storage and add the `JwtTokenProvider` to manage the tokens.
```java
DataStorage tokenStorage = new FileDataStorage(new File(".../MyApi.jwt"));
TokenProvider tokenProvider = new JwtTokenProvider(tokenStorage, onTokenRefresh);

// Configure the HTTP client with Bearer token authorization and enable debugging
HttpClient client = new HttpClient()
    .setAuthorization(new TokenAuthorization("Bearer ", tokenProvider))
    .setDebug(true);
```

### Token Refresh Management

If the token expires or there is no saved token, a token refresh function will be executed. Below is an example of how to implement this logic.
```java
JwtTokenProvider.OnTokenRefresh onTokenRefresh = (TokenProvider provider) -> {
    // Create the request body with the refresh token
    FormBody body = new FormBody()
        .add("token", provider.getRefreshToken())
    ;
    // Create a POST request to refresh the token
    HttpRequest request = new HttpRequest(
        "POST", ".../auth/refresh_token", body);

    // Execute the request with another client to avoid entering a loop
    JSONObject response = request.execute(JSONObject.class);

    // Update the access and refresh tokens
    provider.setAccessToken(response.optString("accessToken"));
    provider.setRefreshToken(response.optString("refreshToken"));
};
```

### Sign In
To sign in and obtain the initial tokens, you can implement a login function like this:
```java
void login(String email, String password) throws Exception {
    // Create the request body with login credentials
    FormBody body = new FormBody()
        .add("email", email)
        .add("password", password)
    ;
    // Create a POST request to the login endpoint
    HttpRequest request = new HttpRequest(
        "POST", ".../auth/login", body);

    // Execute the request with another client to avoid entering a loop
    JSONObject response = request.execute(JSONObject.class);

    // Store the received access and refresh tokens
    tokenProvider.setAccessToken(response.getString("accessToken"));
    tokenProvider.setRefreshToken(response.getString("refreshToken"));
}
```

### Executing Requests with the JWT Token
Once the HTTP client is configured with the JWT token provider, you can execute authenticated requests. Below is an example of how to make a POST request using the JWT token.

```
POST https://postman-echo.com/post HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

```java
public HttpResponse requestWithToken() throws Exception{
  // Create the POST request
  HttpRequest request = new HttpRequest(
      "POST", "https://postman-echo.com/post");

  // Execute the request using the client configured with JWT authorization
  return client.execute(request);
```

### Simple Sign In and Token Refresh

In this example, if there is no token available, the user will log in for the first time. When the token expires, the login process will be repeated to refresh it:

```java
JwtTokenProvider.OnTokenRefresh onTokenRefresh = (TokenProvider provider) -> {
    // Create the request body with login credentials
    FormBody body = new FormBody()
        .add("email", "myMail@domain.com")
        .add("password", "myPassword")
    ;
    // Create a POST request to the login endpoint
    HttpRequest request = new HttpRequest(
        "POST", ".../auth/login", body);

    // Execute the request with another client to avoid entering a loop
    JSONObject response = request.execute(JSONObject.class);

    // Store the new access token
    provider.setAccessToken(response.optString("accessToken"));
};
```


## [GSON](https://github.com/google/gson) 

In your **build.gradle** you will need to add the dependencies for  **GSON**:

```groovy
dependencies {
  ...
  compile 'com.google.code.gson:gson:2.4'
}
```


Now we are ready to start writing some code. The first thing we want to do is define our **Post** model.
Create a new file called **Post.java** and define the **Post** class like this:

```java
public class Post {
  
  @SerializedName("id")
  public long ID;
    
  @SerializedName("date")
  public Date dateCreated;
 
  public String title;
  public String author;
  public String url;
  public String body;
}
```


Let's create a new instance of **GSON** before calling request. We'll also need to set a custom date format on the **GSON** instance to handle the dates returned by the API:

We define the database interactions. They can include a variety of query methods:

```java
public class PostApi {
  HttpClient client = HttpClient.getInstance()
          .setDebug(true);  
    
  public PostApi() {
    Gson gson = new GsonBuilder()
            .setDateFormat("M/d/yy hh:mm a")
            .create();
    
    client.addConverterFactory(new GsonConverterFactory(gson));
  }

  public Async<Post[]> getPosts() {
    HttpRequest request = new HttpRequest(
        "GET", "https://kylewbanks.com/rest/posts.json");

    return client.createAsync(request, Post[].class);
  }

  public Async<String> insert(Post p) {
    RequestBody reqBody = client.createRequestBody(p); // application/json
    // RequestBody reqBody = new FormBody(Maps.fromObjectFields(p)); // application-www-www-form-urlencoded
    // RequestBody reqBody = new MultipartBody(Maps.fromObjectFields(p)); // multipart/form-data
    
    HttpRequest request = new HttpRequest(
            "POST", "https://postman-echo.com/post", reqBody);
    
    return client.createAsync(request, String.class);
  }
}
```

Prepares the request to be executed in the background. Ideal for android applications.
Asynchronously send the request and notify your application with a callback when a response returns.
```java
...
PostApi api = new PostApi();
    
Async<Post[]> async = api.getPosts(); 

async.execute((Post[] response) -> {
  List<Post> list = Arrays.asList(response);
  for (Post post : list) {
    System.out.println(post.title);
  }

}, (Exception e) -> {
  e.printStackTrace(System.out);
});
```

## [Jackson](https://github.com/FasterXML/jackson) 

Now we are ready to start writing some code. The first thing we want to do is define our **Post** model.
Create a new file called **Post.java** and define the **Post** class like this:

```java
public class Post {
    
  @JsonProperty("id")
  public long id;

  @JsonProperty("date")
  public Date dateCreated;

  @JsonProperty("title")
  public String title;
  
  @JsonProperty("author")
  public String author;
  
  @JsonProperty("url")
  public String url;
  
  @JsonProperty("body")
  public String body;
}
```


Let's create a new instance of **ObjectMapper** before calling request. We'll also need to set a custom date format on the **ObjectMapper** instance to handle the dates returned by the API:

We define the database interactions. They can include a variety of query methods:

```java
public class PostApi {
  
  HttpClient client = HttpClient.getInstance()
          .setDebug(true);  
    
  public PostApi() {
    ObjectMapper mapper = new ObjectMapper()
            .setDateFormat(new SimpleDateFormat("M/d/yy hh:mm a"))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    client.addConverterFactory(new JacksonConverterFactory(mapper));
  }

  public Async<Post[]> getPosts() {
    HttpRequest request = new HttpRequest(
        "GET", "https://kylewbanks.com/rest/posts.json");

    return client.createAsync(request, Post[].class);
  }
  
  public Async<String> insert(Post p) {
    // application/json
    RequestBody reqBody = client.createRequestBody(p);
    
    HttpRequest request = new HttpRequest(
            "POST", "https://postman-echo.com/post", reqBody);
    
    return client.createAsync(request, String.class);
  }
}
```

Prepares the request to be executed in the background. Ideal for android applications.
Asynchronously send the request and notify your application with a callback when a response returns.
```java
...
PostApi api = new PostApi();
    
Async<Post[]> async = api.getPosts(); 

async.execute((Post[] response) -> {
  List<Post> list = Arrays.asList(response);
  for (Post post : list) {
    System.out.println(post.title);
  }

}, (Exception e) -> {
  e.printStackTrace(System.out);
});
```

License
=======

    Copyright 2022 Juno Http Client, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
