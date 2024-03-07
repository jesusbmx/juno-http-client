
package json;

import juno.http.HttpClient;
import juno.http.RequestBody;
import java.nio.charset.Charset;
import java.util.Date;

public class JsonRequestBodyTest {
    
    HttpClient cli = HttpClient.getInstance();
    
    public void run() throws Exception {
        Post post = new Post();
        post.id = 7;
        post.dateCreated = new Date();
        post.title = "My Title";
        post.author = "My Author";
        post.url = "http://127.0.0.1";
        post.body = "My body";
        
        RequestBody requestBody = cli.createRequestBody(post);
        requestBody.writeTo(System.out, Charset.forName("UTF-8"));
    }
    
    public static void main(String[] args) throws Exception {
        JsonRequestBodyTest formBodyTest = new JsonRequestBodyTest();
        
        formBodyTest.run();
    }
}
