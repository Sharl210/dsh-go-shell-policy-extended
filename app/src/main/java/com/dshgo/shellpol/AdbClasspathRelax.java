package com.dshgo.shellpol;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Rewrites only the ADB stream opened by the resolved shell connector, never ApplicationInfo. */
final class AdbClasspathRelax {
    private static final String PREFIX="shell:CLASSPATH=";private static final String EXEC=" exec /system/bin/app_process /system/bin ";private AdbClasspathRelax(){}
    static void install(DexKitBridge bridge,ClassLoader loader)throws Exception{MethodData connect=null;for(MethodData method:bridge.findMethod(new FindMethod().matcher(new MethodMatcher().usingEqStrings(PREFIX)))){if(!method.getUsingStrings().contains(EXEC))continue;if(method.getParamTypeNames().size()!=2||!"java.lang.Integer".equals(method.getParamTypeNames().get(0)))continue;if(connect!=null)throw new IllegalStateException("ambiguous ADB shell connector");connect=method;}if(connect==null)throw new IllegalStateException("ADB shell connector missing");MethodData open=null;for(MethodData call:connect.getInvokes()){if(!"openStream".equals(call.getMethodName())||!Arrays.asList("java.lang.String").equals(call.getParamTypeNames()))continue;if(open!=null&&!open.getDescriptor().equals(call.getDescriptor()))throw new IllegalStateException("ambiguous connector stream call");open=call;}if(open==null)throw new IllegalStateException("ADB shell stream call missing");Method connector=connect.getMethodInstance(loader),stream=open.getMethodInstance(loader);final ThreadLocal<Integer> depth=new ThreadLocal<>();XC_MethodHook.Unhook hook=XposedBridge.hookMethod(connector,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){Integer old=depth.get();depth.set(old==null?1:old+1);}@Override protected void afterHookedMethod(MethodHookParam p){Integer n=depth.get();if(n==null||n<=1)depth.remove();else depth.set(n-1);}});try{XposedBridge.hookMethod(stream,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){if(depth.get()==null||!(p.args[0] instanceof String))return;String command=(String)p.args[0],module=HookEntry.moduleApkPath();if(module==null||!command.startsWith(PREFIX)||!command.contains(EXEC))return;int first=command.indexOf('\'',PREFIX.length()),close=first<0?-1:command.indexOf('\'',first+1);if(first<0||close<0)return;String original=command.substring(first+1,close);p.args[0]=PREFIX+quote(module+":"+original)+command.substring(close+1);}});}catch(Throwable f){hook.unhook();throw f;}XposedBridge.log(HookEntry.TAG+": adb_classpath installed "+connect.getDescriptor());}
    static String quote(String value){if(value.indexOf('\0')>=0)throw new IllegalArgumentException("NUL in classpath");return "'"+value.replace("'","'\\''")+"'";}
}
