package juno.http.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import juno.http.Headers;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.io.Files;
import juno.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
        "content": "/Users/.../8eaed87c-8a00-43b7-96f4-7cfef4339359.data"
    }
}
*/
public class CacheModel {

    public final String uuid;
    public long expireAt;
    
    public String requestMethod;
    public String requestUrl;
    
    public int responseCode;
    public Headers responseHeaders = new Headers();
    public File responseContent;
    
    public CacheModel() {
        this.uuid = UUID.randomUUID().toString();
    }
    
    public CacheModel(final Element element) {
        this.uuid = element.getAttribute("uuid");
        this.expireAt = Long.parseLong(element.getAttribute("expireAt"));

        // Request
        Element requestElement = (Element) element.getElementsByTagName("request").item(0);
        this.requestMethod = requestElement.getAttribute("method");
        this.requestUrl = requestElement.getAttribute("url");

        // Response
        Element responseElement = (Element) element.getElementsByTagName("response").item(0);
        this.responseCode = Integer.parseInt(responseElement.getAttribute("code"));
        this.responseContent = new File(responseElement.getAttribute("content"));  
        
        Element headersElement = (Element) responseElement.getElementsByTagName("headers").item(0);
        for (int i = 0; i < headersElement.getChildNodes().getLength(); i++) {
            Node headerNode = headersElement.getChildNodes().item(i);
            if (headerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element headerElement = (Element) headerNode;
                String name = headerElement.getAttribute("name");
                String value = headerElement.getTextContent();
                this.responseHeaders.add(name, value);
            }
        }
    }
    
    public Element toXmlElement(Document doc) {
        Element modelElement = doc.createElement("CacheModel");
        modelElement.setAttribute("uuid", this.uuid);
        modelElement.setAttribute("expireAt", String.valueOf(this.expireAt));

        // Request
        Element requestElement = doc.createElement("request");
        requestElement.setAttribute("method", this.requestMethod);
        requestElement.setAttribute("url", this.requestUrl);
        modelElement.appendChild(requestElement);

        // response
        Element responseElement = doc.createElement("response");
        responseElement.setAttribute("code", String.valueOf(this.responseCode));
        responseElement.setAttribute("content", this.responseContent.getAbsolutePath());

        Element headersElement = doc.createElement("headers");
        for (int i = 0; i < this.responseHeaders.size(); i++) {
            String name = this.responseHeaders.getName(i);
            String value = this.responseHeaders.getValue(i);
            Element headerElement = doc.createElement("header");
            headerElement.setAttribute("name", name);
            headerElement.setTextContent(value);
            headersElement.appendChild(headerElement);
        }
        responseElement.appendChild(headersElement);

        modelElement.appendChild(responseElement);

        return modelElement;
    }
    
    public String getRequestAsString() {
        return requestMethod + " " + requestUrl;
    }

    public void setHttpRequest(HttpRequest request) {
        this.requestMethod = request.getMethod();
        this.requestUrl = request.urlAndParams();
    }
    
    public void writeResponseToFile(HttpResponse response, File tmpData) throws IOException {
        // Write Tmp File
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmpData);
            IOUtils.copy(response.content, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
        
        this.responseCode = response.code;
        this.responseHeaders = response.headers;
        this.responseContent = tmpData;
    }
    
    public HttpResponse getHttpResponseFromFile() throws Exception {
        byte[] content = Files.readByteArray(responseContent);
        return new HttpResponse(
                responseCode, responseHeaders, content);
    }

    @Override
    public String toString() {
        return "CacheModel{" + "uuid=" + uuid + ", expireAt=" + expireAt + ", requestMethod=" + requestMethod + ", requestUrl=" + requestUrl + ", responseCode=" + responseCode + ", responseHeaders=" + responseHeaders + ", responseContent=" + responseContent + '}';
    }
    
    
}
