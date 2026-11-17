package io.github.ccch1mneyyy.dsh_app.phone.shell;

import android.os.Bundle;
import android.os.Parcel;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Minimal wire-compatible implementation used only when the module APK is first on CLASSPATH. */
final class RelaxedShellWire {
    static final byte[] MAGIC = "DSH_ANDROID_SHELL_V1\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    static final int MAX_FRAME = 512 * 1024;
    private RelaxedShellWire() {}
    static Bundle readBundle(InputStream in)throws IOException{byte[] bytes=readFrame(in);if(bytes==null)return null;Parcel p=Parcel.obtain();try{p.unmarshall(bytes,0,bytes.length);p.setDataPosition(0);return p.readBundle(RelaxedShellWire.class.getClassLoader());}finally{p.recycle();}}
    static void writeBundle(OutputStream out,Bundle bundle)throws IOException{Parcel p=Parcel.obtain();byte[] bytes;try{p.writeBundle(bundle);bytes=p.marshall();}finally{p.recycle();}if(bytes.length>MAX_FRAME)throw new IOException("protocolMismatch");out.write((bytes.length>>>24)&255);out.write((bytes.length>>>16)&255);out.write((bytes.length>>>8)&255);out.write(bytes.length&255);out.write(bytes);out.flush();}
    static byte[] readFrame(InputStream in)throws IOException{int a=in.read();if(a<0)return null;int b=in.read(),c=in.read(),d=in.read();if((b|c|d)<0)throw new EOFException("transportLost");int n=(a<<24)|(b<<16)|(c<<8)|d;if(n<0||n>MAX_FRAME)throw new IOException("protocolMismatch");byte[] result=new byte[n];int off=0;while(off<n){int r=in.read(result,off,n-off);if(r<0)throw new EOFException("transportLost");off+=r;}return result;}
    static Bundle result(boolean ok,String error){Bundle b=new Bundle();b.putBoolean("ok",ok);if(error!=null)b.putString("errorCode",error);return b;}
}
