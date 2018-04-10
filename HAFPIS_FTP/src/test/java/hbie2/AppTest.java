package hbie2;

import hbie2.ftp.FTPClientException;
import hbie2.ftp.FTPClientUtil;
import hbie2.nist.NistDecoder;
import hbie2.nist.nistType.NistImg;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
        String host = "172.16.0.193";
        String user = "HAFPIS";
        String pwd = "HAFPIS11";
        FTPClientUtil ftpClient = new FTPClientUtil();
        ftpClient.setHost(host);
        ftpClient.setUsername(user);
        ftpClient.setPassword(pwd);
        String remoteFilePath = "/IMAGE/DIR_0001/R1101000008882017030074.nist";
        String localFilePath = "./test/R1101000008882017030074.nist";
        OutputStream outputStream = new FileOutputStream(new File(localFilePath));
        ftpClient.get(remoteFilePath, outputStream);
        Map<Integer, List<NistImg>> result = NistDecoder.decode(localFilePath);
        System.out.println(result.size());
    }
}
