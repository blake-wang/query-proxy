package com.ijunhai.exception;


public abstract class AbstractException extends Exception {

    private Throwable e;


    public AbstractException(Throwable e) {
        this.e = e;
    }

    @Override
    public String getMessage() {
        return e.getMessage();
    }
}

//public abstract class AbstractException extends Exception {
//    private String message;
//
//    public AbstractException(Throwable e) {
//        this.message = e.getMessage();
//    }
//
//    public AbstractException(String e) {
//        this.message = e;
//    }
//
//    @Override
//    public String getMessage() {
//        return message;
//    }
//}

