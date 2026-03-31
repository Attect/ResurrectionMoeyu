# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

#######################
# AndroidX Core Rules #
#######################
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClass

# Keep AndroidX components
-keep class androidx.** { *; }
-dontwarn androidx.**

##########################
# Live2D SDK Preservation #
##########################
# Preserve Live2D Android library classes
-keep class com.github.hiroshi_nakamura.live2d.** { *; }
-keep class jp.co.live2d.** { *; }
-dontwarn com.github.hiroshi_nakamura.live2d.**
-dontwarn jp.co.live2d.**

# Keep Live2D model and renderer classes
-keep class com.github.hiroshi_nakamura.live2d.cubism.core.** { *; }
-keep class com.github.hiroshi_nakamura.live2d.cubism.framework.** { *; }

############################
# UI Component Preservation #
############################
# Keep Activity and Fragment classes
-keep public class jp.co.a_tm.moeyu.**.*Activity extends android.app.Activity
-keep public class jp.co.a_tm.moeyu.**.*Fragment extends android.app.Fragment
-keep public class jp.co.a_tm.moeyu.**.*Fragment extends androidx.fragment.app.Fragment

# Keep View classes
-keep public class jp.co.a_tm.moeyu.**.*View extends android.view.View

################################
# Network Layer Preservation     #
################################
# Preserve HTTP client components (Apache HttpClient legacy support)
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

# Keep network manager and response handlers
-keep class jp.co.a_tm.moeyu.network.** { *; }

#############################
# Data Layer Preservation     #
#############################
# Preserve model classes and data structures
-keep class jp.co.a_tm.moeyu.model.** { *; }
-keep class jp.co.a_tm.moeyu.data.** { *; }

################################
# Debugging and Optimization   #
################################
# Uncomment the following lines for debugging ProGuard output
#-printseeds seeds.txt
#-printusage usage.txt
#-printmapping mapping.txt

# Preserve line number information for debugging
-keepattributes SourceFile,LineNumberTable

# If needed, hide original source file names
#-renamesourcefileattribute SourceFile
