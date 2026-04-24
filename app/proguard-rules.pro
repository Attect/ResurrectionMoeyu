# Live2D SDK — 本地 JAR，必须完整保留
-keep class jp.live2d.** { *; }
-dontwarn jp.live2d.**

# 序列化模型类（UserData/EventData/GachaResult 等）
-keep class jp.co.a_tm.moeyu.model.** { *; }
-keep class jp.co.a_tm.moeyu.api.model.** { *; }

# 保留行号与注解，便于崩溃分析
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature

# native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}
