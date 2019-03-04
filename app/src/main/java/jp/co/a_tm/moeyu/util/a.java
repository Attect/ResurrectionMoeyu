package jp.co.a_tm.moeyu.util;

/* compiled from: FLI */
public class a {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: jp.co.a_tm.moeyu.util.a.a():java.lang.String, dex:  in method: jp.co.a_tm.moeyu.util.a.a():java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: jp.co.a_tm.moeyu.util.a.a():java.lang.String, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$14.decode(InstructionCodec.java:307)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public java.lang.String a() {
        /*
        // Can't load method instructions: Load method exception: null in method: jp.co.a_tm.moeyu.util.a.a():java.lang.String, dex:  in method: jp.co.a_tm.moeyu.util.a.a():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: jp.co.a_tm.moeyu.util.a.a():java.lang.String");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Incorrect register number in instruction: 0x0000: ARITH  (r15 int) = (r15 int) >> (r4 int), expected to be less than 2
        	at jadx.core.dex.nodes.MethodNode.checkInstructions(MethodNode.java:137)
        	at jadx.core.dex.visitors.blocksmaker.BlockSplitter.visit(BlockSplitter.java:46)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public int b() {
        /*
        r1 = this;
        r15 = r15 >> r4;
        r13 = r153[r210];
        */
        throw new UnsupportedOperationException("Method not decompiled: jp.co.a_tm.moeyu.util.a.b():int");
    }
}
