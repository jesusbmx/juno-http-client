package juno.http.convert;

import juno.http.ResponseBody;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BytesResponseBody extends ResponseBody {
    
    public final byte[] data;

    public BytesResponseBody(ResponseBody body, byte[] data) {
        super(new ByteArrayInputStream(data));
        this.data = data;
        this.charset = body.charset;
        this.code = body.code;
        this.headers.addHeaders(body.headers);
    }

    @Override public byte[] bytes() throws IOException {
        return data;
    }
}
