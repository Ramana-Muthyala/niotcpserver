package ramana.example.niotcpserver.util;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class SslContextUtil {
    private static final String keyStoreFile = "keyStore.jks";
    private static final char[] passphrase = "passphrase".toCharArray();
    private static final String protocol = "TLSv1.2";
    private static KeyManagerFactory kmf;
    private static TrustManagerFactory tmf;
    private static TrustManagerFactory defaultTmf ;
    private static boolean initialized;

    private static void init() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if(initialized) return;

        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");

        ks.load(SslContextUtil.class.getResourceAsStream("/" + keyStoreFile), passphrase);
        ts.load(SslContextUtil.class.getResourceAsStream("/" + keyStoreFile), passphrase);

        kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);
        defaultTmf = TrustManagerFactory.getInstance("SunX509");
        defaultTmf.init((KeyStore) null);

        initialized = true;
    }


    public static SSLContext getContext() throws SSLContextException {
        try {
            init();
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return sslContext;
        } catch (Exception exception) {
            throw new SSLContextException(exception);
        }
    }

    public static SSLContext getContextWithDefaultTrustManagerFactory() throws SSLContextException {
        try {
            init();
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(kmf.getKeyManagers(), defaultTmf.getTrustManagers(), null);
            return sslContext;
        } catch (Exception exception) {
            throw new SSLContextException(exception);
        }
    }

    public static class SSLContextException extends Exception {
        public SSLContextException(Throwable cause) {
            super(cause);
        }
    }
}
