package juno.http.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import juno.http.Headers;
import juno.http.HttpRequest;
import juno.http.ResponseBody;
import juno.http.convert.json.JSON;
import juno.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/*
{
    "uuid": "8eaed87c-8a00-43b7-96f4-7cfef4339359",
    "expireAt": 1691510994652,
    "request": {
        "method": "GET",
        "url": "http://..."
    },
    "response": {
        "code": 200,
        "headers": {
            "X-RateLimit-Remaining": "997",
            "Server": "Apache/2.4.56 ()",
            "ETag": "W/\"4b-tqq2Jh1bYavJL5JOY1zIRJj2Ucw\"",
            "Access-Control-Allow-Origin": "*",
            "Connection": "keep-alive",
            "X-RateLimit-Reset": "1691424869",
            "X-RateLimit-Limit": "1000",
            "Content-Length": "75",
            "Date": "Mon, 07 Aug 2023 16:09:55 GMT",
            "X-Powered-By": "Express",
            "Content-Type": "application/json; charset=utf-8"
        },
        "data": "/Users/.../8eaed87c-8a00-43b7-96f4-7cfef4339359.data"
    }
}
*/
public class CacheModel {

    public String uuid;
    public long expireAt;
    
    public String requestMethod;
    public String requestUrl;
    
    public int responseCode;
    public Headers responseHeaders = new Headers();
    public File responseData;
    
    public CacheModel() {
        
    }
    
    public CacheModel(final JSONObject it) throws JSONException {
        this.uuid = it.getString("uuid");
        this.expireAt = it.getLong("expireAt");
        
        final JSONObject request = it.getJSONObject("request");
        this.requestMethod = request.getString("method");
        this.requestUrl = request.getString("url");
        
        final JSONObject response = it.getJSONObject("response");
        this.responseCode = response.getInt("code");
        
        Map<String, Object> headers = JSON.toMap(
                response.getJSONObject("headers"));
        
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            this.responseHeaders.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        
        final String data = response.getString("data");
        this.responseData = new File(data);
    }
    
    public JSONObject toJson() throws JSONException {
        final JSONObject r = new JSONObject();
        r.put("uuid", this.uuid);
        r.put("expireAt", this.expireAt);
        
        final JSONObject request = new JSONObject();
        request.put("method", this.requestMethod);
        request.put("url", this.requestUrl);
        r.put("request", request);
        
        final JSONObject response = new JSONObject();
        response.put("code", this.responseCode);
        response.put("data", this.responseData.toString());
        
        JSONObject headers = new JSONObject();
        for (int i = 0; i < this.responseHeaders.size(); i++) {
            headers.put(this.responseHeaders.getName(i), 
                    this.responseHeaders.getValue(i));
        }
        response.put("headers", headers);
        
        r.put("response", response);
        
        return r;
    }
    
    public String request() {
        return requestMethod + " " + requestUrl;
    }

    public void setRequest(HttpRequest request) {
        this.requestMethod = request.getMethod();
        this.requestUrl = request.urlAndParams();
    }
    
    public void setResponse(ResponseBody response, File tmpData) throws IOException {
        // Write Tmp File
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmpData);
            IOUtils.copy(response.in, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
        
        this.responseCode = response.code;
        this.responseHeaders = response.headers;
        this.responseData = tmpData;
    }
    
    public ResponseBody getResponseBody() throws Exception {
        ResponseBody body = new ResponseBody(new FileInputStream(responseData));
        body.code = this.responseCode;
        body.headers.addHeaders(this.responseHeaders);
        return body;
    }
}
