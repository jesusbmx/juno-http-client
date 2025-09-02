
import juno.concurrent.Task;
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jesus
 */
public class Test {
    
    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient();

        HttpRequest config = HttpRequest.get(
            "https://postman-echo.com/get")
        ;
        
        Task<String> result = client.newTask(
                config, String.class);
        
        System.out.println(result.sync());
    }
}
