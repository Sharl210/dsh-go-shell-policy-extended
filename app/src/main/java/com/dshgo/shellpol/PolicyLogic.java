package com.dshgo.shellpol;

/** Backend identity gate kept as pure logic; the allow decision itself is fixed. */
public final class PolicyLogic {
    private PolicyLogic() { }
    public static boolean validIdentity(String backend, int uid) {
        return ("adb".equals(backend) && uid == 2000) ||
            ("shizuku".equals(backend) && (uid == 0 || uid == 2000));
    }
}
