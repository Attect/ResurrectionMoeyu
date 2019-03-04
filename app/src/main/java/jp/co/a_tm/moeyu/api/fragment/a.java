package jp.co.a_tm.moeyu.api.fragment;

/* compiled from: EVENT */
public class a {
    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Incorrect register number in instruction: 0x0000: CMP_L  (r218 int) = (r78 long), (r230 long), expected to be less than 2
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
    public java.lang.String a() {
        /*
        r1 = this;
        r218 = (r78 > r230 ? 1 : (r78 == r230 ? 0 : -1));
        r9 = -r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: jp.co.a_tm.moeyu.api.fragment.a.a():java.lang.String");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Incorrect register number in instruction: 0x0000: ARITH  (r2 int) = (r17 int) >>> (r10 int), expected to be less than 2
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
        r2 = r17 >>> r10;
        r12 = (double) r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: jp.co.a_tm.moeyu.api.fragment.a.b():int");
    }
}
