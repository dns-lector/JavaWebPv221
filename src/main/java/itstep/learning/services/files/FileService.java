package itstep.learning.services.files;

import org.apache.commons.fileupload.FileItem;

import java.io.OutputStream;

public interface FileService {
    String upload( FileItem fileItem );
    OutputStream download( String fileName );
}
