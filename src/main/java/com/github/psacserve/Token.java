package com.github.psacserve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.UUID;

public class Token
{
    private static final File token = new File(BanServer.dataFolder, "token.sig");

    public static boolean exists()
    {
        return token.exists();
    }

    public static void genToken()
    {
        if (token.exists())
            return;

        try (FileWriter fw = new FileWriter(token))
        {
            final String token = UUID.randomUUID().toString().replace("-", "") +
                    UUID.randomUUID().toString().replace("-", "");
            fw.write(token + ":" + String.valueOf(new Date().getTime()).hashCode());
        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
        }
    }

    public static String getToken()
    {
        try (BufferedReader r =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(token)
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
