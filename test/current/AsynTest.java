package current;

import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.ResponseBody;
import juno.concurrent.Async;
import juno.concurrent.Callback;

public class AsynTest {
    
  HttpClient cli = HttpClient.getInstance()
            .setDebug(true)
  ;
    
  public Async<ResponseBody> insert(
    String nombre, int edad, boolean soltera) {
      
    FormBody body = new FormBody()
        .add("nombre", nombre)
        .add("edad", edad)
        .add("soltera", soltera);
    
    HttpRequest request = new HttpRequest(
        "POST", "http://127.0.0.1/test.php", body);

    return cli.newAsyncRequest(request, ResponseBody.class);
  }
  
   public void async() {
    Async<ResponseBody> insert = insert(
            "Elizabéth Magaña", 22, true);
            
    insert.execute(new Callback<ResponseBody>() {
        @Override
        public void onResponse(ResponseBody result) throws Exception {
            String str = result.string();
            System.out.println(str);
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
