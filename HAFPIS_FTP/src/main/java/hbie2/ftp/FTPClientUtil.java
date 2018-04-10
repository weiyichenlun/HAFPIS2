package hbie2.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

/**
 * FTP客户端

 */
public class FTPClientUtil {
    private final org.slf4j.Logger log = LoggerFactory.getLogger(FTPClientUtil.class);

    private ThreadLocal<FTPClient> ftpClientThreadLocal = new ThreadLocal<>();

    private String host;
    private int port;
    private String username;
    private String  password;
    private boolean binaryTransfer = true;
    private boolean passiveMode = true;
    private String encoding = "UTF-8";
    private int clientTimeout = 1000 * 30;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBinaryTransfer() {
        return binaryTransfer;
    }

    public void setBinaryTransfer(boolean binaryTransfer) {
        this.binaryTransfer = binaryTransfer;
    }

    public boolean isPassiveMode() {
        return passiveMode;
    }

    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    /**
     * 返回一个FTPClient实例
     *
     * @throws FTPClientException
     */
    private FTPClient getFTPClient() throws FTPClientException {
        if (ftpClientThreadLocal.get() != null && ftpClientThreadLocal.get().isConnected()) {
            return ftpClientThreadLocal.get();
        } else {
            FTPClient ftpClient = new FTPClient(); //构造一个FtpClient实例
//            ftpClient.setControlEncoding(encoding); //设置字符集
            connect(ftpClient); //连接到ftp服务器

            //设置为passive模式
            if (passiveMode) {
                ftpClient.enterLocalPassiveMode();
            }
            setFileType(ftpClient); //设置文件传输类型

            try {
                ftpClient.setSoTimeout(clientTimeout);
            } catch (SocketException e) {
                throw new FTPClientException("Set timeout error.", e);
            }
            ftpClientThreadLocal.set(ftpClient);
            return ftpClient;
        }
    }

    /**
     * 设置文件传输类型
     *
     * @throws FTPClientException
     * @throws IOException
     */
    private void setFileType(FTPClient ftpClient) throws FTPClientException {
        try {
            if (binaryTransfer) {
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
            }
        } catch (IOException e) {
            throw new FTPClientException("Could not to set file type.", e);
        }
    }

    /**
     * 连接到ftp服务器
     *
     * @param ftpClient
     * @return 连接成功返回true，否则返回false
     * @throws FTPClientException
     */
    private boolean connect(FTPClient ftpClient) throws FTPClientException {
        try {
//            ftpClient.connect(host, port);
            ftpClient.connect(host);

            // 连接后检测返回码来校验连接是否成功
            int reply = ftpClient.getReplyCode();

            if (FTPReply.isPositiveCompletion(reply)) {
                //登陆到ftp服务器
                if (ftpClient.login(username, password)) {
                    setFileType(ftpClient);
                    return true;
                }
            } else {
                ftpClient.disconnect();
                throw new FTPClientException("FTP server refused connection.");
            }
        } catch (IOException e) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect(); //断开连接
                } catch (IOException e1) {
                    throw new FTPClientException("Could not disconnect from server.", e1);
                }

            }
            throw new FTPClientException("Could not connect to server.", e);
        }
        return false;
    }


    //---------------------------------------------------------------------
    // public method
    //---------------------------------------------------------------------
    /**
     * 断开ftp连接
     *
     * @throws FTPClientException
     */
    public void disconnect() throws FTPClientException {
        try {
            FTPClient ftpClient = getFTPClient();
            ftpClient.logout();
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
                ftpClient = null;
            }
        } catch (IOException e) {
            throw new FTPClientException("Could not disconnect from server.", e);
        }
    }

    public boolean mkdir(String pathname) throws FTPClientException {
        return mkdir(pathname, null);
    }

    /**
     * 在ftp服务器端创建目录（不支持一次创建多级目录）
     *
     * 该方法执行完后将自动关闭当前连接
     *
     * @param pathname
     * @return
     * @throws FTPClientException
     */
    public boolean mkdir(String pathname, String workingDirectory) throws FTPClientException {
        return mkdir(pathname, workingDirectory, true);
    }

    /**
     * 在ftp服务器端创建目录（不支持一次创建多级目录）
     *
     * @param pathname
     * @param autoClose 是否自动关闭当前连接
     * @return
     * @throws FTPClientException
     */
    public boolean mkdir(String pathname, String workingDirectory, boolean autoClose) throws FTPClientException {
        try {
            getFTPClient().changeWorkingDirectory(workingDirectory);
            return getFTPClient().makeDirectory(pathname);
        } catch (IOException e) {
            throw new FTPClientException("Could not mkdir.", e);
        } finally {
            if (autoClose) {
                disconnect(); //断开连接
            }
        }
    }

    /**
     * 上传一个本地文件到远程指定文件
     *
     * @param remoteAbsoluteFile 远程文件名(包括完整路径)
     * @param localAbsoluteFile 本地文件名(包括完整路径)
     * @return 成功时，返回true，失败返回false
     * @throws FTPClientException
     */
    public boolean put(String remoteAbsoluteFile, String localAbsoluteFile) throws FTPClientException {
        return put(remoteAbsoluteFile, localAbsoluteFile, true);
    }

    /**
     * 上传一个本地文件到远程指定文件
     *
     * @param remoteAbsoluteFile 远程文件名(包括完整路径)
     * @param localAbsoluteFile 本地文件名(包括完整路径)
     * @param autoClose 是否自动关闭当前连接
     * @return 成功时，返回true，失败返回false
     * @throws FTPClientException
     */
    public boolean put(String remoteAbsoluteFile, String localAbsoluteFile, boolean autoClose) throws FTPClientException {
        InputStream input = null;
        try {
            // 处理传输
            input = new FileInputStream(localAbsoluteFile);
            getFTPClient().storeFile(remoteAbsoluteFile, input);
            log.debug("put " + localAbsoluteFile);
            return true;
        } catch (FileNotFoundException e) {
            throw new FTPClientException("local file not found.", e);
        } catch (IOException e) {
            throw new FTPClientException("Could not put file to server.", e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                throw new FTPClientException("Couldn't close FileInputStream.", e);
            }
            if (autoClose) {
                disconnect(); //断开连接
            }
        }
    }

    /**
     * 下载一个远程文件到本地的指定文件
     *
     * @param remoteAbsoluteFile 远程文件名(包括完整路径)
     * @param localAbsoluteFile 本地文件名(包括完整路径)
     * @return 成功时，返回true，失败返回false
     * @throws FTPClientException
     */
    public boolean get(String remoteAbsoluteFile, String localAbsoluteFile) throws FTPClientException {
        return get(remoteAbsoluteFile, localAbsoluteFile, true);
    }

    /**
     * 下载一个远程文件到本地的指定文件
     *
     * @param remoteAbsoluteFile 远程文件名(包括完整路径)
     * @param localAbsoluteFile 本地文件名(包括完整路径)
     * @param autoClose 是否自动关闭当前连接
     *
     * @return 成功时，返回true，失败返回false
     * @throws FTPClientException
     */
    public boolean get(String remoteAbsoluteFile, String localAbsoluteFile, boolean autoClose) throws FTPClientException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(localAbsoluteFile);
            return get(remoteAbsoluteFile, output, autoClose);
        } catch (FileNotFoundException e) {
            throw new FTPClientException("local file not found.", e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                throw new FTPClientException("Couldn't close FileOutputStream.", e);
            }
        }
    }

    /**
     * 下载一个远程文件到指定的流 处理完后记得关闭流
     *
     * @param remoteAbsoluteFile
     * @param output
     * @return
     * @throws FTPClientException
     */
    public boolean get(String remoteAbsoluteFile, OutputStream output) throws FTPClientException {
        return get(remoteAbsoluteFile, output, true);
    }

    /**
     * 下载一个远程文件到指定的流 处理完后记得关闭流
     *
     * @param remoteAbsoluteFile
     * @param output
     * @param autoClose
     * @return
     * @throws FTPClientException
     */
    public boolean get(String remoteAbsoluteFile, OutputStream output, boolean autoClose) throws FTPClientException {
        try {
            FTPClient ftpClient = getFTPClient();
            // 处理传输
            return ftpClient.retrieveFile(remoteAbsoluteFile, output);
        } catch (IOException e) {
            throw new FTPClientException("Couldn't get file from server.", e);
        } finally {
            if (autoClose) {
                disconnect(); //关闭链接
            }
        }
    }

    /**
     * 从ftp服务器上删除一个文件
     * 该方法将自动关闭当前连接
     *
     * @param delFile
     * @return
     * @throws FTPClientException
     */
    public boolean delete(String delFile) throws FTPClientException {
        return delete(delFile, true);
    }

    /**
     * 从ftp服务器上删除一个文件
     *
     * @param delFile
     * @param autoClose 是否自动关闭当前连接
     *
     * @return
     * @throws FTPClientException
     */
    public boolean delete(String delFile, boolean autoClose) throws FTPClientException {
        try {
            getFTPClient().deleteFile(delFile);
            return true;
        } catch (IOException e) {
            throw new FTPClientException("Couldn't delete file from server.", e);
        } finally {
            if (autoClose) {
                disconnect(); //关闭链接
            }
        }
    }

    /**
     * 批量删除
     * 该方法将自动关闭当前连接
     *
     * @param delFiles
     * @return
     * @throws FTPClientException
     */
    public boolean delete(String[] delFiles) throws FTPClientException {
        return delete(delFiles, true);
    }

    /**
     * 批量删除
     *
     * @param delFiles
     * @param autoClose 是否自动关闭当前连接
     *
     * @return
     * @throws FTPClientException
     */
    public boolean delete(String[] delFiles, boolean autoClose) throws FTPClientException {
        try {
            FTPClient ftpClient = getFTPClient();
            for (String s : delFiles) {
                ftpClient.deleteFile(s);
            }
            return true;
        } catch (IOException e) {
            throw new FTPClientException("Couldn't delete file from server.", e);
        } finally {
            if (autoClose) {
                disconnect(); //关闭链接
            }
        }
    }

    /**
     * 列出远程默认目录下所有的文件
     *
     * @return 远程默认目录下所有文件名的列表，目录不存在或者目录下没有文件时返回0长度的数组
     * @throws FTPClientException
     */
    public String[] listNames() throws FTPClientException {
        return listNames(null, true);
    }

    public String[] listNames(boolean autoClose) throws FTPClientException {
        return listNames(null, autoClose);
    }

    /**
     * 列出远程目录下所有的文件
     *
     * @param remotePath 远程目录名
     * @param autoClose 是否自动关闭当前连接
     *
     * @return 远程目录下所有文件名的列表，目录不存在或者目录下没有文件时返回0长度的数组
     * @throws FTPClientException
     */
    public String[] listNames(String remotePath, boolean autoClose) throws FTPClientException {
        try {
            String[] listNames = getFTPClient().listNames(remotePath);
            return listNames;
        } catch (IOException e) {
            throw new FTPClientException("列出远程目录下所有的文件时出现异常", e);
        } finally {
            if (autoClose) {
                disconnect(); //关闭链接
            }
        }
    }

    public static void main(String[] args) throws FTPClientException, InterruptedException {
//        FTPClientUtil ftp = new FTPClientUtil();
//        ftp.setHost("localhost");
//        ftp.setPort(2121);
//        ftp.setUsername("admin");
//        ftp.setPassword("admin");
//        ftp.setBinaryTransfer(false);
//        ftp.setPassiveMode(false);
//        ftp.setEncoding("utf-8");
//
//        boolean ret = ftp.put("/group/tbdev/query/user-upload/12345678910.txt", "D:/099_temp/query/12345.txt");
//        System.out.println(ret);
//        ftp.mkdir("asd", "user-upload");
//
//        ftp.disconnect();
//        ftp.mkdir("user-upload1");
//        ftp.disconnect();
//
//        String[] aa = {"/group/tbdev/query/user-upload/123.txt", "/group/tbdev/query/user-upload/SMTrace.txt"};
//        ftp.delete(aa);
    }
}
