package juno.http.convert;

import juno.http.Headers;
import juno.http.ResponseBody;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import juno.io.Files;
import juno.io.IOUtils;

public class FileResponseBodyConvert implements ResponseBodyConvert<File> {

  private File dir;
  private String name;

  public FileResponseBodyConvert setDir(File dir) {
    this.dir = dir;
    return this;
  }
  
  public FileResponseBodyConvert setDir(Object dir) {
    return this.setDir(new File(dir.toString()));
  }

  public FileResponseBodyConvert setName(Object name) {
    this.name = name.toString();
    return this;
  }
  
  public File newFile(Headers headers) throws IOException {
    String fileName = name;
    
    if (fileName == null) {
      fileName = headers.getFileName();
      
      if (fileName == null) {
        fileName = "file_" + System.currentTimeMillis() + ".download";
      }
    }
        
    if (dir != null) {
      dir.mkdirs();
      return new File(dir, fileName);
    }
    
    final String baseName = Files.getBaseName(fileName);
    final String extension = Files.getExtension(fileName);
    return File.createTempFile(baseName + "_", "." + extension);
  }
  
  @Override
  public File parse(ResponseBody respBody) throws Exception {
    BufferedOutputStream bos = null;
    try {
      File f = newFile(respBody.headers);
      bos = new BufferedOutputStream(new FileOutputStream(f));
      respBody.writeTo(bos);
      return f;
    } finally {
      IOUtils.closeQuietly(bos);
      respBody.close();
    }
  }
}
