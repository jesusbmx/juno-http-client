package convert.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpTask;
import juno.http.RequestBody;
import juno.http.convert.gson.GsonConverterFactory;
// import com.squareup.okhttp.OkHttpClient;
// import java.io.IOException;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import restlight.BasicHttpStack;
public class PostDao {
  
  HttpClient client = HttpClient.getInstance()
          .setDebug(true);  
    
  public PostDao() {
    Gson gson = new GsonBuilder()
            .setDateFormat("M/d/yy hh:mm a")
            .create();
    
    client.addConverterFactory(new GsonConverterFactory(gson));
  }

  public HttpTask<Post[]> getPosts() {
    HttpRequest request = HttpRequest.get(
            "https://kylewbanks.com/rest/posts.json");

    return client.newTask(request, Post[].class);
  }

  public HttpTask<String> insert(Post p) {
    RequestBody reqBody = client.createRequestBody(p);

    HttpRequest request = HttpRequest.post(
            "https://postman-echo.com/post", reqBody);

    return client.newTask(request, String.class);
  }

  //  BasicHttpStack stack = new BasicHttpStack() {  
  //    final OkHttpClient client = new OkHttpClient();
  //    @Override
  //    public HttpURLConnection open(URL src) throws IOException {
  //      return client.open(src);
  //    }
  //  };
}
