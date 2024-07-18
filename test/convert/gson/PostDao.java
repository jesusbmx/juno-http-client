package convert.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import juno.concurrent.Async;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.RequestBody;
import juno.http.convert.gson.GsonConvertFactory;
// import com.squareup.okhttp.OkHttpClient;
// import java.io.IOException;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import restlight.BasicHttpStack;
public class PostDao {
  
  HttpClient cli = HttpClient.getInstance()
          .setDebug(true);  
    
  public PostDao() {
    Gson gson = new GsonBuilder()
            .setDateFormat("M/d/yy hh:mm a")
            .create();
    
    cli.setFactory(new GsonConvertFactory(gson));
  }

  public Async<Post[]> getPosts() {
    HttpRequest request = new HttpRequest(
        "GET", "https://kylewbanks.com/rest/posts.json");

    return cli.createAsync(request, Post[].class);
  }
  
  public Async<String> insert(Post p) {
    RequestBody reqBody = cli.createRequestBody(p);
    
    HttpRequest request = new HttpRequest(
            "POST", "https://postman-echo.com/post", reqBody);
    
    return cli.createAsync(request, String.class);
  }

  //  BasicHttpStack stack = new BasicHttpStack() {  
  //    final OkHttpClient client = new OkHttpClient();
  //    @Override
  //    public HttpURLConnection open(URL src) throws IOException {
  //      return client.open(src);
  //    }
  //  };
}
