package hbie2.ftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/10
 * 最后修改时间:2018/4/10
 */
public class FtpClient {
    public static boolean downloadFile(String hostname, int port, String username, String password, String pathname,
                                       String filename, InputStream iputstream) {
        boolean flag = false;
        FTPClient client = new FTPClient();
        try {
            client.connect(hostname, port);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
