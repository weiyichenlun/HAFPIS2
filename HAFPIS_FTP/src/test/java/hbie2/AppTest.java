package hbie2;

import hbie2.ftp.FTPClientException;
import hbie2.ftp.FTPClientUtil;
import hbie2.nist.NistDecoder;
import hbie2.nist.nistType.NistImg;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws FileNotFoundException, FTPClientException {
        String host = "test001";
        String user = "HAFPIS";
        String pwd = "Qwsazx001";
        FTPClientUtil ftpClient = new FTPClientUtil();
        ftpClient.setHost(host);
        ftpClient.setUsername(user);
        ftpClient.setPassword(pwd);
        String remoteFilePath = "/IMAGE/DIR_0001/R1101000008882017030074.nist";
        String localPath = System.getProperty("user.dir").replace("\\", "\\\\");
        System.out.println("localpath is " + localPath);
        String localFilePath = localPath + "\\\\" + "R1101000008882017030074.nist";
        OutputStream outputStream = new FileOutputStream(new File(localFilePath));
        String filename = "R1100000008882018040001.nist";
        boolean is = ftpClient.get(".", filename, outputStream);
        System.out.println(is?"success":"failed");
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String ftpUrl = "ftp://%s:%s@%s/%s;type=i";
//        ftpUrl = String.format(ftpUrl, user, pwd, host, filename);
//        try {
//            URL url = new URL(ftpUrl);
//            URLConnection connection = url.openConnection();
//            InputStream inputStream = connection.getInputStream();
//            FileOutputStream fos = new FileOutputStream(new File(localFilePath));
//            byte[] buffer = new byte[1024];
//            int byteRead = -1;
//            while ((byteRead = inputStream.read(buffer)) != -1) {
//                fos.write(buffer, 0, byteRead);
//            }
//
//            outputStream.flush();
//            outputStream.close();
//            inputStream.close();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Map<Integer, List<NistImg>> result = NistDecoder.decode(localFilePath);
        System.out.println(result.size());
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
