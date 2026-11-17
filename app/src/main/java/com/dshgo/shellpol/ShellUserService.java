package com.dshgo.shellpol;
import android.os.Bundle;import android.os.IBinder;import android.os.Binder;import android.os.Parcel;import java.lang.reflect.Method;
/** Shizuku UserService Binder; intentionally not an Android Service component. */
public final class ShellUserService extends Binder{private static final String D="io.github.ccch1mneyyy.dsh_app.phone.shell.IAndroidShell";private static final int EXEC=IBinder.FIRST_CALL_TRANSACTION,DESTROY=16777115;private static final Class<?> MAIN;
 static{try{MAIN=Class.forName("io.github.ccch1mneyyy.dsh_app.phone.shell.AndroidShellMain");}catch(ClassNotFoundException e){throw new ExceptionInInitializerError(e);}}
 public ShellUserService(){attachInterface(null,D);}
 private static Bundle execute(Bundle r)throws Exception{Method m=MAIN.getDeclaredMethod("handleExternal",Bundle.class);m.setAccessible(true);return (Bundle)m.invoke(null,r);}
 private static void destroy()throws Exception{Method m=MAIN.getDeclaredMethod("destroyExternal");m.setAccessible(true);m.invoke(null);}
 @Override protected boolean onTransact(int code,Parcel data,Parcel reply,int flags){try{if(code==DESTROY){data.enforceInterface(D);destroy();reply.writeNoException();System.exit(0);return true;}if(code!=EXEC)return super.onTransact(code,data,reply,flags);data.enforceInterface(D);Bundle r=execute(data.readBundle(getClass().getClassLoader()));reply.writeNoException();reply.writeBundle(r);return true;}catch(Throwable e){return false;}}
}
