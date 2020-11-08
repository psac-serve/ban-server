package com.github.psacserve.Response;

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
