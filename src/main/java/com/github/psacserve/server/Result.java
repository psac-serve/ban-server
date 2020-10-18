package com.github.psacserve.server;

public class Result
{
    public String body;
    public int code;
    public Result(String body, int code)
    {
        this.body = body;
        this.code = code;
    }
}
