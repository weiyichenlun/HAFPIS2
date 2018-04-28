package hbie2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpClientProvider;
import sun.net.ftp.FtpProtocolException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        String host = "172.16.0.214";
        String usr = "HAFPIS";
        String pwd = "Qwsazx001";
        String path = "E:\\\\USERS\\\\HAFPIS.COMM_GJ\\\\DBCAP\\\\";
        FtpClient client = FtpClientProvider.provider().createFtpClient();
        InetSocketAddress socketAddress = new InetSocketAddress(host, 21);
        try {
            client.connect(socketAddress);
            client.login(usr, pwd.toCharArray());
            System.out.println(client.getSystem());
            System.out.println(client.getWorkingDirectory());
            System.out.println(client.getLastFileName());
            File file = new File("./test.nist");
            FileOutputStream fos = new FileOutputStream(file);
            client.getFile("R1103000008882018040011.nist", fos);
            fos.flush();
//            client.changeDirectory(path);
            System.out.println((client.getWelcomeMsg()));
        } catch (FtpProtocolException | IOException e) {
            e.printStackTrace();
        }
        assertTrue( true );
    }
}
