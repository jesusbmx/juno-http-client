package jackson;

import java.util.Date;
import juno.concurrent.Async;
import juno.concurrent.Callback;
import juno.http.Response;

public class JacksonTest {

  PostDao dao = new PostDao();  
    
  public void list() {
    Async<Response<Post[]>> call = dao.getPosts(); 
    
    call.execute(new Callback<Response<Post[]>>() {
      @Override
      public void onResponse(Response<Post[]> response) throws Exception {
        //List<Post> list = Arrays.asList(result);
        for (Post post : response.result) {
          System.out.println(post.title);
        }
      }
      @Override
      public void onFailure(Exception e) {
        e.printStackTrace(System.out);
      }
    });
  }  
  
  public void insert() {
    Post post = new Post();
    post.id = 7;
    post.dateCreated = new Date();
    post.title = "My Title";
    post.author = "My Author";
    post.url = "http://127.0.0.1";
    post.body = "My body";
    
    Async<Response<String>> async = dao.insert(post); 
    async.execute(new Callback<Response<String>>() {
      @Override
      public void onResponse(Response<String> response) throws Exception {
          System.out.println(response.result);
      }
      @Override
      public void onFailure(Exception e) {
        e.printStackTrace(System.out);
      }
    });
  }
    
  public static void main(String[] args) {
    JacksonTest gsonTest = new JacksonTest();
    gsonTest.list();
    //gsonTest.insert();
  }
}
