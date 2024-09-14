package com.ethereum.exceptions;

public class ParserException extends Exception{
    public ParserException(int line, String message){
        super("On line: " + line + ": " + message);
    }

    public ParserException(int line, String message, Throwable err){
        super("On line: " + line + ": " + message,err);
    }
}
