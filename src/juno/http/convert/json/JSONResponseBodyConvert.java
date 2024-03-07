package juno.http.convert.json;

import org.json.JSONArray;
import org.json.JSONObject;
import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;

public class JSONResponseBodyConvert implements ResponseBodyConvert<JSON>{

    @Override
    public JSON parse(ResponseBody respBody) throws Exception {
        try {
            String json = respBody.string();
            return JSON.of(json);
        } finally {
            respBody.close();
        }
    }    
    
    public static class Obj implements ResponseBodyConvert<JSONObject>{

        @Override
        public JSONObject parse(ResponseBody respBody) throws Exception {
            try {
                String json = respBody.string();
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
                String json = respBody.string();
                return JSON.newJSONArray(json);
            } finally {
                respBody.close();
            }
        }    
    }

}
