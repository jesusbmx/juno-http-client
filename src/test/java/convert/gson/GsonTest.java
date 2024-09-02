package convert.gson;

import java.util.Date;
import juno.concurrent.Async;
import juno.concurrent.Callback;

public class GsonTest {

  PostDao dao = new PostDao();  
    
  public void list() {
    Async<Post[]> call = dao.getPosts(); 
    
    call.execute(new Callback<Post[]>() {
      @Override
      public void onResponse(Post[] response) throws Exception {
        //List<Post> list = Arrays.asList(result);
        for (Post post : response) {
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
    
    Async<String> call = dao.insert(post); 
    
    call.execute(new Callback<String>() {
      @Override
      public void onResponse(String response) throws Exception {
          System.out.println(response);
      }
      @Override
      public void onFailure(Exception e) {
        e.printStackTrace(System.out);
      }
    });
  }
    
  public static void main(String[] args) {
    GsonTest gsonTest = new GsonTest();
    gsonTest.list();
    //gsonTest.insert();
  }
}
