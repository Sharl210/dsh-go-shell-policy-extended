package io.github.ccch1mneyyy.dsh_app.phone.shell;

import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/** Classpath-first replacement for the APK's guarded job launcher. */
public final class AndroidShellJobMain {
    private AndroidShellJobMain() {}
    public static void main(String[] args)throws Exception{
        if(args==null||args.length!=2)System.exit(125);long timeout;try{timeout=Long.parseLong(args[0]);}catch(NumberFormatException e){System.exit(125);return;}if(timeout<100L||args[1].indexOf('\0')>=0)System.exit(125);int pid=android.os.Process.myPid();Os.setsid();FileOutputStream out=new FileOutputStream(java.io.FileDescriptor.out);out.write(("DSH_SHELL_JOB "+pid+"\n").getBytes(StandardCharsets.US_ASCII));out.flush();if(System.in.read()!=1)System.exit(125);ProcessBuilder childBuilder=new ProcessBuilder("/system/bin/sh","-c",args[1]);shellEnvironment(childBuilder.environment());Process child=childBuilder.redirectInput(new File("/dev/null")).inheritIO().start();Thread deadline=new Thread(()->{try{Thread.sleep(timeout);kill(child,pid);}catch(InterruptedException ignored){}} ,"dsh-shell-child-deadline");deadline.setDaemon(true);deadline.start();Thread parentWatch=new Thread(()->{try{if(System.in.read()<0)kill(child,pid);}catch(Throwable ignored){}} ,"dsh-shell-parent-watch");parentWatch.setDaemon(true);parentWatch.start();System.exit(child.waitFor());
    }
    private static void shellEnvironment(java.util.Map<String,String> env){java.util.Map<String,String> inherited=new java.util.HashMap<>(System.getenv());env.clear();env.put("PATH","/system/bin:/system/xbin:/vendor/bin");env.put("HOME","/data/local/tmp");env.put("TMPDIR","/data/local/tmp");env.put("LANG","C.UTF-8");String[] roots={"ANDROID_ROOT","ANDROID_DATA","ANDROID_ART_ROOT","ANDROID_I18N_ROOT","ANDROID_TZDATA_ROOT"};for(String key:roots){String value=inherited.get(key);if(value!=null&&value.startsWith("/"))env.put(key,value);}}
    private static void kill(Process child,int pid){try{Os.kill(-pid,OsConstants.SIGKILL);}catch(Throwable ignored){}child.destroy();}
}
