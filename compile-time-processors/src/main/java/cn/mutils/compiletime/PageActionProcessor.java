package cn.mutils.compiletime;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
            "#foreach($project in $DEPEND_LIST)" +
            "    sActionMap.putAll(new $PACKAGE_NAME.${CLASS_NAME}$project());\n" +
            "#end\n" +
            "  }\n\n" +
            "  @Override\n" +
            "  public Class<?> getPage(String action) {\n" +
            "    return sActionMap.get(action);\n" +
            "  }\n\n}\n";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        PageActionProject project = null;
        HashMap<String, String> actionMap = new HashMap<String, String>();
        for (Element element : roundEnv.getElementsAnnotatedWith(PageAction.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement classElement = (TypeElement) element;
            PageActionProject elementProject = classElement.getAnnotation(PageActionProject.class);
            if (elementProject != null) {
                project = elementProject;
                continue;
            }
            String action = classElement.getAnnotation(PageAction.class).value();
            String page = classElement.getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + ":" + page + "(\"" + action + "\") ");
            actionMap.put(action, page);
        }
        if (project == null || project.name().isEmpty()) {
            return true;
        }
        VelocityEngine ve = new VelocityEngine();

        // Generate map code for each project
        String pName = IPageActionMap.class.getPackage().getName() + ".impl";
        String cName = IPageActionMap.class.getSimpleName().substring(1) + "Impl" + project.name();
        VelocityContext vc = new VelocityContext();
        vc.put("PACKAGE_NAME", pName);
        vc.put("CLASS_NAME", cName);
        vc.put("ACTION_MAP", actionMap);
        createSourceFile(cName, ve, vc, TEMPLATE_MAP_LIBRARY);

        if (project.type() != PageActionProject.ProjectType.APPLICATION) {
            return true;
        }

        // Generate map code for all projects
        List<String> dependenciesList = new ArrayList<String>();
        for (String libName : project.dependencies()) {
            dependenciesList.add(libName);
        }
        dependenciesList.add(project.name());
        String cNameApp = IPageActionMap.class.getSimpleName().substring(1) + "Impl";
        VelocityContext vcApp = new VelocityContext();
        vcApp.put("PACKAGE_NAME", pName);
        vcApp.put("CLASS_NAME", cNameApp);
        vcApp.put("DEPEND_LIST", dependenciesList);
        vcApp.put("INTERFACE_NAME", IPageActionMap.class.getName());
        createSourceFile(cNameApp, ve, vcApp, TEMPLATE_MAP_APPLICATION);
        return true;
    }

    private void createSourceFile(String name, VelocityEngine engine, VelocityContext context, String template) {
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
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + ":" + e);
            System.exit(0);
        } finally {
            try {
                if (fileWrite != null) {
                    fileWrite.close();
                }
            } catch (Exception ex) {
                messager.printMessage(Diagnostic.Kind.NOTE, TAG + ":" + ex);
                System.exit(0);
            }
        }
    }

}
