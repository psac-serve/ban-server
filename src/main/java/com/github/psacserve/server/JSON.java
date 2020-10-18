package com.github.psacserve.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.psacserve.BanServer;

import java.util.HashMap;

public class JSON
{
    public static String response(boolean success, String message)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(new HashMap<String, Object>(){{put("success", success); put("message", message);}});
        }
        catch(Exception e)
        {
            BanServer.printStackTrace(e);
            return "";
        }
    }

}
