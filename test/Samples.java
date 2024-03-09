
import java.io.File;
import juno.http.FormBody;
import juno.http.Headers;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpUrl;
import juno.http.MultipartBody;
import juno.http.RequestBody;
import juno.http.ResponseBody;
import juno.http.convert.generic.FileResponseBodyConvert;

public class Samples {

    HttpClient client = HttpClient.getInstance()
            .setDebug(true);

    String get() throws Exception {
        HttpRequest request = new HttpRequest(
                "GET", "https://postman-echo.com/get");

        return client.execute(request, String.class);
    }

    String post(int id, String name, boolean active) throws Exception {
        // application-www-www-form-urlencoded
        FormBody reqBody = new FormBody()
                .add("id", id)
                .add("name", name)
                .add("active", active)
        ;
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", reqBody);

        return client.execute(request, String.class);
    }

    File download() throws Exception {
        HttpRequest request = new HttpRequest(
                "GET", "https://github.com/jesusbmx/java-http-client/raw/master/dist/juno-http-client.jar")
                .setTimeoutMs(20000);

        FileResponseBodyConvert convert = new FileResponseBodyConvert()
                .setDir(System.getProperty("user.home") + "\\Downloads\\") //.setName("httpclient.jar")
        ;
        return client.execute(request, convert);
        //return client.execute(request, File.class);
    }

    String upload(File file) throws Exception {
        // multipart/form-data
        MultipartBody reqBody = new MultipartBody()
                .addParam("name", "John Doe")
                .addFile("file", file)
        ;
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", reqBody);

        return client.execute(request, String.class);
    }
    
    String request() throws Exception {
        String json = "{\"id\": \"7\", \"name\": \"bar\", \"active\": \"true\"}";
        
        // application/json
        RequestBody reqBody = RequestBody.create(
                "application/json", json);
        
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", reqBody);

        return client.execute(request, String.class);
    }
    
    ResponseBody getIpLocation() throws Exception { 
        HttpUrl url = new HttpUrl("http://ip-api.com/{returnType}/{ip}")
            .setPath("returnType", "json")
            .setPath("ip", "24.48.0.1")
            .addQueryParameter("fields", "status,message,query,country,city")
            .addQueryParameter("lang", "en")
      ;
      HttpRequest request = new HttpRequest("GET", url);

      Headers headers = new Headers()
            .add("User-Agent: nombre-cliente")
      ;
      request.setHeaders(headers);

      return client.execute(request, ResponseBody.class);
    }

    public static void main(String[] args) throws Exception {
        Samples samples = new Samples();
        //System.out.println(samples.get());
        //System.out.println(samples.post(7, "bar", true));
        
        //File f = samples.download();
        //System.out.println(f);
        //System.out.println(samples.upload(f));
        
        System.out.println(samples.request());
        //System.out.println(samples.getIpLocation().readString());
    }
}
