package itstep.learning.services.files;

import org.apache.commons.fileupload.FileItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public interface FileService {
    String upload( FileItem fileItem );
    OutputStream download( String fileName ) throws IOException;
}
