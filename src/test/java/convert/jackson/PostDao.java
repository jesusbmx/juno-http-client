package convert.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import juno.concurrent.Task;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResult;
import juno.http.RequestBody;
import juno.http.convert.jackson.JacksonConverterFactory;
// import com.squareup.okhttp.OkHttpClient;
// import java.io.IOException;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import restlight.BasicHttpStack;
public class PostDao {
  
  HttpClient client = HttpClient.getInstance()
          .setDebug(true);  
    
  public PostDao() {
    ObjectMapper mapper = new ObjectMapper()
            .setDateFormat(new SimpleDateFormat("M/d/yy hh:mm a"))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    client.addConverterFactory(new JacksonConverterFactory(mapper)); 
  }

  public Task<HttpResult<Post[]>> getPosts() {
    HttpRequest request = HttpRequest.get(
        "https://kylewbanks.com/rest/posts.json");

    return client.newTask(request, Post[].class);
  }

  public Task<HttpResult<String>> insert(Post p) {
    RequestBody reqBody = client.createRequestBody(p);
    //RequestBody reqBody = new FormBody(Maps.fromObjectFields(p));
    //RequestBody reqBody = new MultipartBody(Maps.fromObjectFields(p));

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
