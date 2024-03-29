/**The MIT License (MIT)
Copyright (c) 2018 by AleksanderSergeevich
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.soaesps.core.Utils;

import org.apache.commons.math3.random.RandomGenerator;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptoHelper {
    private static final Logger logger = Logger.getLogger(CryptoHelper.class.getName());
    
    static public byte[] serializeObject(final Serializable obj) throws IOException {
        byte[] bytes = null;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)
        ) {
            oos.writeObject(obj);
            oos.flush();
            bytes = baos.toByteArray();
        }
        return bytes;
    }

    static public Object deserializeObject(final byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
            ObjectInputStream oos = new ObjectInputStream(baos)) {
            return oos.readObject();
        }
    }

    public static String getObjectDigest(final Serializable obj) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] objDigest = md.digest(serializeObject(obj));
        return DatatypeConverter.printHexBinary(objDigest);
    }

    public static BigInteger getObjectChecksum(final Serializable obj) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] objDigest = md.digest(serializeObject(obj));
        return new BigInteger(1, objDigest);
    }

    static public String getUuuid() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(UUID.randomUUID().toString().getBytes("UTF-8"));
        return DatatypeConverter.printHexBinary(md.digest());
    }

    static public byte[] signMessage(String msg, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return signMessage(msg.getBytes(), key);
    }

    static public byte[] signMessage(ByteBuffer msg, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return signMessage(msg.array(), key);
    }

    static public byte[] signMessage(byte[] msg, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance("SHA1withRSA");
        rsa.initSign(key);
        rsa.update(msg);
        return rsa.sign();
    }

    static public boolean verifySignature(ByteBuffer signToVerify, ByteBuffer signedData, final PublicKey key, String algorithmName) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return verifySignature(signToVerify.array(), signedData.array(), key, algorithmName);
    }

    static public boolean verifySignature(final byte[] signToVerify, final byte[] signedData, final PublicKey key, String algorithmName) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance(algorithmName);
        sign.initVerify(key);
        sign.update(signedData);
        return sign.verify(signToVerify);
    }

    static public KeyPair generate_RSA() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            keyPair = kpg.generateKeyPair();
            PublicKey pub = keyPair.getPublic();
            Key pvt = keyPair.getPrivate();
        }
        catch(NoSuchAlgorithmException nsaex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "NoSuchAlgorithmException occur : ", nsaex);
            }
        }
        return keyPair;
    }

    static public void writeKey(String fileName, Key key) {
        try(FileOutputStream out = new FileOutputStream(fileName)) {
            out.write(key.getEncoded());
        }
        catch(FileNotFoundException fnfex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "FileNotFoundException occur : ", fnfex);
            }
        }
        catch(IOException ioex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "IOException occur : ", ioex);
            }
        }
    }

    static public void writePublicKeyAsText(String fileName, PublicKey publicKey) {
        try(Writer out = new FileWriter(fileName)) {
            Base64.Encoder encoder = Base64.getEncoder();
            out.write("-----BEGIN RSA PUBLIC KEY-----\n");
            out.write(encoder.encodeToString(publicKey.getEncoded()));
            out.write("\n-----END RSA PUBLIC KEY-----\n");
        }
        catch(FileNotFoundException fnfex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "FileNotFoundException occur : ", fnfex);
            }
        }
        catch(IOException ioex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "IOException occur : ", ioex);
            }
        }
    }

    static public void writePrivateKeyAsText(String fileName, PrivateKey key) {
        try(Writer out = new FileWriter(fileName)) {
            Base64.Encoder encoder = Base64.getEncoder();
            out.write("-----BEGIN RSA PRIVATE KEY-----\n");
            out.write(encoder.encodeToString(key.getEncoded()));
            out.write("\n-----END RSA PRIVATE KEY-----\n");
        }
        catch(FileNotFoundException fnfex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "FileNotFoundException occur : ", fnfex);
            }
        }
        catch(IOException ioex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "IOException occur : ", ioex);
            }
        }
    }

    static public PrivateKey bytesToPrivateKey(final byte[] bytes) {
        try {
            PKCS8EncodedKeySpec pkcs8ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyf.generatePrivate(pkcs8ks);
            return privateKey;
        }
        catch(final NoSuchAlgorithmException nsae) {
            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "NoSuchAlgorithmException occurred: ", nsae);
            }
        }
        catch(final InvalidKeySpecException ikse) {
            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "InvalidKeySpecException occurred: ", ikse);
            }
        }
        return null;
    }

    static public PrivateKey loadPrivatekey(String fileName) {
        Path path = Paths.get(fileName);
        PrivateKey key = null;
        try {
            byte[] bytes = Files.readAllBytes(path);
            key = bytesToPrivateKey(bytes);
        }
        catch(final IOException ioex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "IOException occur : ", ioex);
            }
        }
        return key;
    }

    static public PublicKey bytesToPublicKey(final byte[] bytes) {
        try {
            X509EncodedKeySpec x509eks = new X509EncodedKeySpec(bytes);
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyf.generatePublic(x509eks);
            return publicKey;
        }
        catch(final NoSuchAlgorithmException nsae) {
            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "NoSuchAlgorithmException occurred: ", nsae);
            }
        }
        catch(final InvalidKeySpecException ikse) {
            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "InvalidKeySpecException occurred: ", ikse);
            }
        }
        return null;
    }

    static public PublicKey loadPublickey(String fileName) {
        Path path = Paths.get(fileName);
        PublicKey key = null;
        try {
            byte[] bytes = Files.readAllBytes(path);
            key = bytesToPublicKey(bytes);
        }
        catch(final IOException ioex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "IOException occur : ", ioex);
            }
        }
        return key;
    }

    public String genSecretStr(PrivateKey privateKey, String secret, Integer randMaxSeed, Integer randStrSize) throws IOException {
        final Long creationTime = System.currentTimeMillis();
        RandomGenerator rng = HashGeneratorHelper.getRandomGenerator(Math
                .toIntExact(creationTime % randMaxSeed));
        try {
            final String randStr = HashGeneratorHelper.getRandSequence(rng, randStrSize);
            final String mixStr = HashGeneratorHelper.mixTwoString(secret, randStr);
            final byte[] sign = CryptoHelper.signMessage(mixStr.concat(":" + creationTime), privateKey);

            return DatatypeConverter.printHexBinary(sign).concat(":").concat(randStr).concat(":" + creationTime);

        } catch (final NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            throw new IOException(ex);
        }
    }
}