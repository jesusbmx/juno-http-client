
import java.io.File;
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.MultipartBody;
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

    public static void main(String[] args) throws Exception {
        Samples samples = new Samples();
        //System.out.println(samples.get());
        System.out.println(samples.post(7, "bar", true));
        
        //File f = samples.download();
        //System.out.println(f);
        //System.out.println(samples.upload(f));
    }
}
