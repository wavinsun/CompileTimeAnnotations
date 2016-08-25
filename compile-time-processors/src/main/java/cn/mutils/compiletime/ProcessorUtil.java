package cn.mutils.compiletime;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Options;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

/**
 * Created by wenhua.ywh on 2016/8/24.
 */
public class ProcessorUtil {

    public static boolean createSourceFile(ProcessingEnvironment processingEnv, String name, VelocityEngine engine, VelocityContext context, String template) {
        Messager messager = processingEnv.getMessager();
        StringWriter writer = new StringWriter();
        engine.evaluate(context, writer, "", template);
        String code = writer.toString();
        messager.printMessage(Diagnostic.Kind.NOTE, name + ":\n" + code);
        Writer fileWrite = null;
        try {
            fileWrite = processingEnv.getFiler().createSourceFile(name).openWriter();
            fileWrite.write(code);
            fileWrite.flush();
            return true;
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, name + ":" + e);
            return false;
        } finally {
            try {
                if (fileWrite != null) {
                    fileWrite.close();
                }
            } catch (Exception ex) {
                messager.printMessage(Diagnostic.Kind.ERROR, name + ":" + ex);
            }
        }
    }

    public static String getBuildTarget(ProcessingEnvironment processingEnv) {
        if (!(processingEnv instanceof JavacProcessingEnvironment)) {
            return null;
        }
        JavacProcessingEnvironment JavacEnv = (JavacProcessingEnvironment) processingEnv;
        String classPath = Options.instance(JavacEnv.getContext()).get("-d");
        if (classPath == null) {
            return null;
        }
        int index = classPath.lastIndexOf("build");
        index = index == -1 ? classPath.lastIndexOf("bin") : index;
        return index == -1 ? null : new File(classPath.substring(0, index)).getName();
    }

    public static String getTargetName(String target) {
        StringBuilder sb = new StringBuilder();
        boolean toUpperCase = true;
        for (int i = 0, size = target.length(); i < size; i++) {
            char c = target.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                sb.append(toUpperCase ? Character.toUpperCase(c) : c);
                toUpperCase = false;
            } else {
                toUpperCase = true;
                if (i != 0) {
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

}
