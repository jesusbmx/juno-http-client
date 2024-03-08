package current;

import juno.concurrent.Async;
import juno.concurrent.Callback;
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.Response;

public class AsynTest {
    
  HttpClient cli = HttpClient.getInstance()
            .setDebug(true)
  ;
    
  public Async<Response<String>> insert(
    String nombre, int edad, boolean soltera) {
      
    FormBody body = new FormBody()
        .add("nombre", nombre)
        .add("edad", edad)
        .add("soltera", soltera);
    
    HttpRequest request = new HttpRequest(
        "POST", "http://127.0.0.1/test.php", body);

    return cli.newAsyncRequest(request, String.class);
  }
  
   public void async() {
    Async<Response<String>> insert = insert(
            "Elizabéth Magaña", 22, true);
            
    insert.execute(new Callback<Response<String>>() {
        @Override
        public void onResponse(Response<String> response) throws Exception {
            System.out.println(response.result);
        }
        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
        }
    });
  }
  
//  public void async() {
//    Task<ResponseBody> insert = insert(
//            "Elizabéth Magaña", 22, true);
//    
//    insert.execute(new HttpCallback<ResponseBody>() {
//
//      @Override
//      public void onResponse(ResponseBody result) throws Exception {
//        String str = result.string();
//        System.out.println(str);
//      }
//        
//      @Override
//      public void onFailure(Exception e) {
//        e.printStackTrace();
//      }
//    });
//  }
  
//  public void sync() {
//    Task<ResponseBody> insert = insert(
//            "Elizabéth Magaña", 22, true);
//    
//    try (ResponseBody body = insert.execute()) {
//      System.out.println(body.string());
//    
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
//  }
  
  public static void main(String[] args) {
    AsynTest test = new AsynTest();
     
    test.async();
  }
}
