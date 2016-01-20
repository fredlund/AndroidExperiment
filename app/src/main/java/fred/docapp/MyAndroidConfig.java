package fred.docapp;

import java.security.Provider;
import java.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.AndroidConfig;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.signature.SignatureDSA;
import net.schmizz.sshj.signature.SignatureRSA;
import net.schmizz.sshj.transport.random.JCERandom;
import net.schmizz.sshj.transport.random.SingletonRandomFactory;

/**
 * Created by fred on 20/01/16.
 */
public class MyAndroidConfig extends DefaultConfig {
   static {
        final Logger log = LoggerFactory.getLogger(AndroidConfig.class);
        SecurityUtils.setRegisterBouncyCastle(false);
        try {
            Class<?> bcpClazz = Class.forName("org.spongycastle.jce.provider.BouncyCastleProvider");
            Security.addProvider((Provider) bcpClazz.newInstance());
            SecurityUtils.setSecurityProvider("SC");
            log.info("SpongyCastle set");
        } catch (ClassNotFoundException e) {
            log.info("SpongyCastle was not found.");
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException", e);
        } catch (InstantiationException e) {
            log.info("InstantiationException", e);
        }
    }

    // don't add ECDSA
    protected void initSignatureFactories() {
       setSignatureFactories(new SignatureRSA.Factory(), new SignatureDSA.Factory());
    }

    @Override
    protected void initRandomFactory(boolean ignored) {
        setRandomFactory(new SingletonRandomFactory(new JCERandom.Factory()));
    }
}

