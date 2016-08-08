package cn.mutils.compiletime;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by wenhua.ywh on 2016/8/5.
 */
@SupportedAnnotationTypes("cn.mutils.compiletime.PageAction")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PageActionProcessor extends AbstractProcessor {

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
            messager.printMessage(Diagnostic.Kind.NOTE, PageActionProcessor.class.getSimpleName()
                    + ":" + classElement.getQualifiedName() + "(\"" + classElement.getAnnotation(PageAction.class).value() + "\") ");
            actionMap.put(classElement.getAnnotation(PageAction.class).value(), classElement.getQualifiedName().toString());
        }
        if (project == null || project.name().isEmpty()) {
            return true;
        }

        // Generate map code for each project
        String pName = IPageActionMap.class.getPackage().getName() + ".impl";
        String cName = IPageActionMap.class.getSimpleName().substring(1) + "Impl" + project.name();
        StringBuilder code = new StringBuilder();
        code.append("// Generated code from PageActionProcessor. Do not modify!\n// Email: wenhua.ywh@alibaba-inc.com\npackage ");
        code.append(pName);
        code.append(";\n\n");
        code.append("public final class ");
        code.append(cName);
        code.append(" extends " + HashMap.class.getName() + "<String, Class<?>> {\n\n  public ");
        code.append(cName);
        code.append("() {");
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : actionMap.entrySet()) {
            if (isFirst) {
                isFirst = false;
                code.append("\n");
            }
            code.append("    put(\"" + entry.getKey() + "\", " + entry.getValue() + ".class);\n");
        }
        code.append("  }\n\n}");
        String codeString = code.toString();
        messager.printMessage(Diagnostic.Kind.NOTE, cName + ":\n" + codeString);
        try {
            JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(cName);
            Writer writer = fileObject.openWriter();
            writer.write(codeString);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.NOTE, PageActionProcessor.class.getSimpleName() + ":" + e);
            System.exit(0);
        }

        if (project.type() != PageActionProject.ProjectType.APPLICATION) {
            return true;
        }

        // Generate map code for all projects
        List<String> dependenciesList = new ArrayList<String>();
        for (String libName : project.dependencies()) {
            dependenciesList.add(libName);
        }
        dependenciesList.add(project.name());
        String pNameApp = pName;
        String cNameApp = IPageActionMap.class.getSimpleName().substring(1) + "Impl";
        StringBuilder codeApp = new StringBuilder();
        codeApp.append("// Generated code from PageActionProcessor. Do not modify!\n// Email: wenhua.ywh@alibaba-inc.com\npackage ");
        codeApp.append(pNameApp);
        codeApp.append(";\n\npublic final class ");
        codeApp.append(cNameApp);
        codeApp.append(" implements " + IPageActionMap.class.getName() + " {\n\n");
        codeApp.append("  private static final " + Map.class.getName() + "<String, Class<?>> sActionMap = new " + HashMap.class.getName());
        codeApp.append("<String, Class<?>>();\n  static {\n");
        for (String name : dependenciesList) {
            codeApp.append("    sActionMap.putAll(new " + pName + "." + cNameApp + name + "());\n");
        }
        codeApp.append("  }\n\n  @Override\n  public Class<?> getPage(String action) {\n    return sActionMap.get(action);\n  }\n\n}");
        String codeAppString = codeApp.toString();
        messager.printMessage(Diagnostic.Kind.NOTE, cNameApp + ":\n" + codeAppString);
        try {
            JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(cNameApp);
            Writer writer = fileObject.openWriter();
            writer.write(codeAppString);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.NOTE, PageActionProcessor.class.getSimpleName() + ":" + e);
            System.exit(0);
        }
        return true;
    }

}
