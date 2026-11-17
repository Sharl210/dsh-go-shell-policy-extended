package com.dshgo.shellpol;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Application-process validators only; does not change remote host limits or identity checks. */
final class ShellProtocolRelax {
    private ShellProtocolRelax() { }
    static void install(DexKitBridge bridge,ClassLoader loader)throws Exception{
        List<MethodData> commands=shaped(bridge,"invalidCommand","java.lang.String"),timeouts=shaped(bridge,"invalidTimeout","long");MethodData command=null,timeout=null;int pairs=0;
        for(MethodData c:commands)for(MethodData t:timeouts)if(c.getDeclaredClass().getName().equals(t.getDeclaredClass().getName())){command=c;timeout=t;pairs++;}
        XposedBridge.log(HookEntry.TAG+": protocol_candidates command="+commands.size()+" timeout="+timeouts.size()+" shared_owner_pairs="+pairs);if(pairs!=1)throw new IllegalStateException("protocol shared-owner pair count="+pairs);
        Method cm=command.getMethodInstance(loader),tm=timeout.getMethodInstance(loader);XC_MethodHook.Unhook ch=XposedBridge.hookMethod(cm,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){p.setResult(ShellRequestPolicy.commandError((String)p.args[0]));}});
        try{XposedBridge.hookMethod(tm,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){p.setResult(ShellRequestPolicy.timeoutError((Long)p.args[0]));}});}catch(Throwable f){ch.unhook();throw f;}
        XposedBridge.log(HookEntry.TAG+": protocol_validators installed command="+command.getDescriptor()+" timeout="+timeout.getDescriptor());
    }
    private static List<MethodData> shaped(DexKitBridge b,String a,String p){List<MethodData>r=new ArrayList<>();for(MethodData m:b.findMethod(new FindMethod().matcher(new MethodMatcher().usingEqStrings(a))))if("java.lang.String".equals(m.getReturnTypeName())&&Arrays.asList(p).equals(m.getParamTypeNames()))r.add(m);return r;}
}
