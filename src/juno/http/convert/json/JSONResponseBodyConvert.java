package juno.http.convert.json;

import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;
import juno.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONResponseBodyConvert implements ResponseBodyConvert<JSON>{

    @Override
    public JSON parse(ResponseBody respBody) throws Exception {
        try {
            byte[] data = IOUtils.readByteArray(respBody.in);
            String json = new String(data, respBody.charset);
            return JSON.of(json);
            
        } finally {
            respBody.close();
        }
    }    
    
    public static class Obj implements ResponseBodyConvert<JSONObject>{

        @Override
        public JSONObject parse(ResponseBody respBody) throws Exception {
            try {
                byte[] data = IOUtils.readByteArray(respBody.in);
                String json = new String(data, respBody.charset);
                //System.out.println(json);
                return JSON.newJSONObject(json);
                
            } finally {
                respBody.close();
            }
        }    
    }

    public static class Array implements ResponseBodyConvert<JSONArray>{

        @Override
        public JSONArray parse(ResponseBody respBody) throws Exception {
            try {
                byte[] data = IOUtils.readByteArray(respBody.in);
                String json = new String(data, respBody.charset);
                return JSON.newJSONArray(json);
                
            } finally {
                respBody.close();
            }
        }    
    }

}
