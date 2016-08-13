package cn.mutils.compiletime;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Options;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by wenhua.ywh on 2016/8/5.
 */
@SupportedAnnotationTypes("cn.mutils.compiletime.PageAction")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PageActionProcessor extends AbstractProcessor {

    private static final String TAG = PageActionProcessor.class.getSimpleName();

    private static final String TEMPLATE_MAP_LIBRARY = "// Generate code from " + TAG + ". Do not modify!\n" +
            "// Email: wavinsun@qq.com\n" +
            "package $PACKAGE_NAME;\n\n" +
            "import java.util.HashMap;\n\n" +
            "@" + PageActionReport.class.getName() + "(\n" +
            "  actions = {\n" +
            "#foreach($action in ${ACTION_MAP.keySet()})" +
            "    \"${action}\", \n" +
            "#end\n" +
            "  },\n" +
            "  pages = {\n" +
            "#foreach($action in ${ACTION_MAP.keySet()})" +
            "    \"${ACTION_MAP.get(${action})}\",\n" +
            "#end\n" +
            "  }\n)\n" +
            "public final class $CLASS_NAME extends HashMap<String, Class<?>> {\n\n" +
            "  public $CLASS_NAME() {\n" +
            "#foreach($action in ${ACTION_MAP.keySet()})" +
            "    put(\"${action}\", ${ACTION_MAP.get(${action})}.class);\n" +
            "#end\n" +
            "  }\n\n}\n";
    private static final String TEMPLATE_MAP_APPLICATION = "// Generated code from " + TAG + ". Do not modify!\n" +
            "// Email: wavinsun@qq.com\n" +
            "package $PACKAGE_NAME;\n\n" +
            "import java.util.HashMap;\n\n" +
            "public final class $CLASS_NAME implements $INTERFACE_NAME {\n\n" +
            "  private static final HashMap<String, Class<?>> sActionMap = new HashMap<String, Class<?>>();\n" +
            "  static {\n" +
            "#foreach($map in $DEPEND_MAPS)" +
            "    sActionMap.putAll(new $map());\n" +
            "#end\n" +
            "  }\n\n" +
            "  @Override\n" +
            "  public Class<?> getPage(String action) {\n" +
            "    return sActionMap.get(action);\n" +
            "  }\n\n}\n";

    private boolean mProcessed;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (mProcessed) {
            return true;
        }
        Messager messager = processingEnv.getMessager();
        String target = getBuildTarget();
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + ":" + target);
        boolean isMaster = false;
        HashMap<String, String> actionMap = new HashMap<String, String>();
        for (Element element : roundEnv.getElementsAnnotatedWith(PageAction.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement classElement = (TypeElement) element;
            PageAction pageAction = classElement.getAnnotation(PageAction.class);
            if (pageAction.master()) {
                isMaster = true;
            }
            String action = pageAction.value();
            if (action.isEmpty()) {
                continue;
            }
            String page = classElement.getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + ":" + page + "(\"" + action + "\") ");
            if (actionMap.containsKey(action)) {
                messager.printMessage(Diagnostic.Kind.ERROR, TAG + ": duplicate action: " + action +
                        "\n" + page + ".class" + "\n" + actionMap.get(action) + ".class");
                mProcessed = true;
                return true;
            }
            actionMap.put(action, page);
        }
        VelocityEngine ve = new VelocityEngine();
        String pName = IPageActionMap.class.getPackage().getName() + ".impl";
        String cNameApp = IPageActionMap.class.getSimpleName().substring(1) + "Impl";
        String cName = cNameApp + getTargetName(target);
        if (!actionMap.isEmpty()) {
            // Generate map code for each project
            VelocityContext vc = new VelocityContext();
            vc.put("PACKAGE_NAME", pName);
            vc.put("CLASS_NAME", cName);
            vc.put("ACTION_MAP", actionMap);
            if (!createSourceFile(cName, ve, vc, TEMPLATE_MAP_LIBRARY)) {
                mProcessed = true;
                return true;
            }
        }
        if (!isMaster) {
            mProcessed = true;
            return true;
        }

        // Review
        ArrayList<String> dependMaps = new ArrayList<String>();
        ArrayList<Map<String, String>> projectActionMaps = new ArrayList<Map<String, String>>();
        PackageElement pElement = processingEnv.getElementUtils().getPackageElement(pName);
        if (pElement != null) {
            for (Element element : pElement.getEnclosedElements()) {
                if (element.getKind() != ElementKind.CLASS) {
                    continue;
                }
                TypeElement classElement = (TypeElement) element;
                String simpleName = classElement.getSimpleName().toString();
                if (!simpleName.startsWith(cNameApp)) {
                    continue;
                }
                String superClass = classElement.getSuperclass().toString();
                if (!superClass.startsWith(HashMap.class.getName())) {
                    continue;
                }
                PageActionReport report = classElement.getAnnotation(PageActionReport.class);
                if (report == null) {
                    continue;
                }
                String scannedMap = classElement.getQualifiedName().toString();
                dependMaps.add(scannedMap);
                messager.printMessage(Diagnostic.Kind.NOTE, TAG + ": scanned: " + scannedMap);
                Map<String, String> map = new HashMap<String, String>();
                for (int i = report.actions().length - 1; i >= 0; i--) {
                    map.put(report.actions()[i], report.pages()[i]);
                }
                projectActionMaps.add(map);
            }
        }
        if (!actionMap.isEmpty()) {
            projectActionMaps.add(actionMap);
            dependMaps.add(pName + "." + cName);
        }
        for (int i = 0, size = projectActionMaps.size(); i < size; i++) {
            Map<String, String> map = projectActionMaps.get(i);
            for (int j = 1; j < size; j++) {
                if (i == j) {
                    break;
                }
                Map<String, String> mapAnother = projectActionMaps.get(j);
                for (String action : map.keySet()) {
                    if (mapAnother.containsKey(action)) {
                        messager.printMessage(Diagnostic.Kind.ERROR, TAG + ": duplicate action: " + action +
                                "\n" + map.get(action) + ".class" + "\n" + mapAnother.get(action) + ".class");
                        mProcessed = true;
                        return true;
                    }
                }
            }
        }

        // Generate map code for all projects
        VelocityContext vc = new VelocityContext();
        vc.put("PACKAGE_NAME", pName);
        vc.put("CLASS_NAME", cNameApp);
        vc.put("DEPEND_MAPS", dependMaps);
        vc.put("INTERFACE_NAME", IPageActionMap.class.getName());
        if (!createSourceFile(cNameApp, ve, vc, TEMPLATE_MAP_APPLICATION)) {
            mProcessed = true;
            return true;
        }
        mProcessed = true;
        return true;
    }

    private boolean createSourceFile(String name, VelocityEngine engine, VelocityContext context, String template) {
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
            messager.printMessage(Diagnostic.Kind.ERROR, TAG + ":" + e);
            return false;
        } finally {
            try {
                if (fileWrite != null) {
                    fileWrite.close();
                }
            } catch (Exception ex) {
                messager.printMessage(Diagnostic.Kind.ERROR, TAG + ":" + ex);
            }
        }
    }

    private String getBuildTarget() {
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

    private static String getTargetName(String target) {
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
