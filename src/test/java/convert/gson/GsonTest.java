package convert.gson;

import java.util.Date;
import juno.concurrent.Callback;
import juno.http.HttpResult;
import juno.http.HttpTask;

public class GsonTest {

  PostDao dao = new PostDao();

  public void list() {
    HttpTask<Post[]> task = dao.getPosts();

    task.async(new Callback<HttpResult<Post[]>>() {
      @Override
      public void onResponse(HttpResult<Post[]> result) throws Exception {
        for (Post post : result.data) {
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

    HttpTask<String> task = dao.insert(post);

    task.async(new Callback<HttpResult<String>>() {
      @Override
      public void onResponse(HttpResult<String> result) throws Exception {
          System.out.println(result.data);
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
