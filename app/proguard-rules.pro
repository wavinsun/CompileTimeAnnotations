# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontshrink

-dontoptimize

-keep @proguard.annotation.Keep class *

-keepclassmembers class * {
    @proguard.annotation.Keep *;
}


# @KeepName specifies not to optimize or obfuscate the annotated class or
# class member as an entry point.

-keepnames @proguard.annotation.KeepName class *

-keepclassmembernames class * {
    @proguard.annotation.KeepName *;
}


# The following annotations can only be specified with classes.

# @KeepImplementations and @KeepPublicImplementations specify to keep all,
# resp. all public, implementations or extensions of the annotated class as
# entry points. Note the extension of the java-like syntax, adding annotations
# before the (wild-carded) interface name.

-keep        class * implements @proguard.annotation.KeepImplementations       *
-keep public class * implements @proguard.annotation.KeepPublicImplementations *

# @KeepApplication specifies to keep the annotated class as an application,
# together with its main method.

-keepclasseswithmembers @proguard.annotation.KeepApplication public class * {
    public static void main(java.lang.String[]);
}

# @KeepClassMembers, @KeepPublicClassMembers, and
# @KeepPublicProtectedClassMembers specify to keep all, all public, resp.
# all public or protected, class members of the annotated class from being
# shrunk, optimized, or obfuscated as entry points.

-keepclassmembers @proguard.annotation.KeepClassMembers class * {
    *;
}

-keepclassmembers @proguard.annotation.KeepPublicClassMembers class * {
    public *;
}

-keepclassmembers @proguard.annotation.KeepPublicProtectedClassMembers class * {
    public protected *;
}

# @KeepClassMemberNames, @KeepPublicClassMemberNames, and
# @KeepPublicProtectedClassMemberNames specify to keep all, all public, resp.
# all public or protected, class members of the annotated class from being
# optimized or obfuscated as entry points.

-keepclassmembernames @proguard.annotation.KeepClassMemberNames class * {
    *;
}

-keepclassmembernames @proguard.annotation.KeepPublicClassMemberNames class * {
    public *;
}

-keepclassmembernames @proguard.annotation.KeepPublicProtectedClassMemberNames class * {
    public protected *;
}

# @KeepGettersSetters and @KeepPublicGettersSetters specify to keep all, resp.
# all public, getters and setters of the annotated class from being shrunk,
# optimized, or obfuscated as entry points.

-keepclassmembers @proguard.annotation.KeepGettersSetters class * {
    void set*(***);
    void set*(int, ***);

    boolean is*();
    boolean is*(int);

    *** get*();
    *** get*(int);
}

-keepclassmembers @proguard.annotation.KeepPublicGettersSetters class * {
    public void set*(***);
    public void set*(int, ***);

    public boolean is*();
    public boolean is*(int);

    public *** get*();
    public *** get*(int);
}