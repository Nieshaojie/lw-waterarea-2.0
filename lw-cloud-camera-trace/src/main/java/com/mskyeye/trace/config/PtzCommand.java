package com.mskyeye.trace.config;

public enum PtzCommand {
    UP(0), DOWN(1), LEFT(2), RIGHT(3),
    ZOOM_IN(11), ZOOM_OUT(12),
    FOCUS_IN(13), FOCUS_OUT(14);

    public final int code;

    PtzCommand(int code) {
        this.code = code;
    }
}
