package com.dshgo.shellpol;

/** Validation shared by the parser hook and the module's helper protocol. */
final class ShellRequestPolicy {
    static final long DEFAULT_TIMEOUT_MS = 30_000L;
    private ShellRequestPolicy() { }
    static String commandError(String command) { if(command==null||command.trim().isEmpty()||command.indexOf('\0')>=0)return "invalidCommand";return null; }
    static String timeoutError(long timeoutMs) { return timeoutMs<100L||timeoutMs>Long.MAX_VALUE-2000L?"invalidTimeout":null; }
    static Long timeout(Object value) { if(value instanceof Byte||value instanceof Short||value instanceof Integer||value instanceof Long){long m=((Number)value).longValue();return timeoutError(m)==null?m:null;}return null; }
}
