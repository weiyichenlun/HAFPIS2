package hbie2.nist.nativesdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by pms on 2017/4/11.
 */
public class SdkConfig {
    public static String decodeFilePath;


    private Properties prop = new Properties();


    public SdkConfig(File file) throws IOException {
       prop.load(new FileInputStream(file));
        decodeFilePath=prop.getProperty("decodeFilePath");
    }
}
