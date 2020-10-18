package com.github.psacserve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Security
{
    private static final File key = new File("security.key");
    private static final File pub = new File("security.pub.key");

    public static boolean exists()
    {
        return key.exists();
    }

    public static void genKey()
    {
        try(FileWriter writer = new FileWriter(key);
            FileWriter pb = new FileWriter(pub))
        {
            KeyPair pair = genKeyPair(BanServer.config.get("security.length"));
            writer.write("-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()) + "\n" +
                    "-----END PRIVATE KEY-----");
            pb.write(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        }
        catch(Exception e)
        {
            BanServer.printStackTrace(e);
        }
    }

    public static KeyPair genKeyPair(int leng) throws NoSuchAlgorithmException
    {
        KeyPairGenerator key = KeyPairGenerator.getInstance("RSA");
        key.initialize(leng);
        return key.generateKeyPair();
    }

    public static String getKey(String file)
    {
        try(BufferedReader r =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(
                                            new File(file)
                                    )
                            )
                    )
        )
        {
            StringBuilder a = new StringBuilder();
            String text;
            while ((text = r.readLine()) != null)
                a.append(text);
            return a.toString();
        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
            return "";
        }
    }
}
