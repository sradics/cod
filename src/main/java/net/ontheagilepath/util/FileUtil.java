package net.ontheagilepath.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sebastianradics on 05.03.17.
 */
public class FileUtil {
    public static String loadFileToString(Class base, String fileName){

        InputStream is = base.getResourceAsStream(fileName);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[10000];
        while(true){
            int bytesRead = 0;
            try {
                bytesRead = is.read(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (bytesRead==-1)
                break;
            bos.write(data,0,bytesRead);
        }
        data = bos.toByteArray();
        return new String(data);
    }
}
