package com.releasingcode.goldenlobby.exception;


@SuppressWarnings("serial")
public class IncompatibleMinecraftVersionException extends RuntimeException {
    public IncompatibleMinecraftVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleMinecraftVersionException(Throwable cause) {
        super(cause);
    }
}
