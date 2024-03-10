package juno.http.convert.json;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConvert;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONResponseBodyConvert implements ResponseBodyConvert<JSON>{

    @Override
    public JSON parse(HttpResponse response) throws Exception {
        try {
            String json = response.readString();
            return JSON.of(json);
            
        } finally {
            response.close();
        }
    }    
    
    public static class Obj implements ResponseBodyConvert<JSONObject>{

        @Override
        public JSONObject parse(HttpResponse response) throws Exception {
            try {
                String json = response.readString();
                //System.out.println(json);
                return JSON.newJSONObject(json);
                
            } finally {
                response.close();
            }
        }    
    }

    public static class Array implements ResponseBodyConvert<JSONArray>{

        @Override
        public JSONArray parse(HttpResponse response) throws Exception {
            try {
               String json = response.readString();
                return JSON.newJSONArray(json);
                
            } finally {
                response.close();
            }
        }    
    }

}
