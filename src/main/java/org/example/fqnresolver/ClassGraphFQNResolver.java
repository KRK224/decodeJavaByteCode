package org.example.fqnresolver;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import java.util.ArrayList;
import java.util.List;


/**
 * 우선 필요한 package만 그때 그때 scan해서 사용하는데 추후에는 자체 프로젝트 package와 외부 라이브러리에서 사용 중인 package 및 클래스만 사용하여
 * classGraph 구성할 것
 */
public class ClassGraphFQNResolver {

    private List<String> classPathList;

    public ClassGraphFQNResolver(List<String> classPathList) {
        this.classPathList = classPathList;
    }


    public List<String> getClassFQNListByPackageName(String packageName) {
        List<String> classFQNList = new ArrayList<>();
        ClassGraph cg = new ClassGraph().enableClassInfo().enableFieldInfo().enableMethodInfo()
            .ignoreClassVisibility().ignoreFieldVisibility().ignoreFieldVisibility();

        for (String classPath : classPathList) {
            cg.overrideClasspath(classPath);
        }
        cg.acceptPackages(packageName);

        try (ScanResult scanResult = cg.scan()) {
            scanResult.getAllClasses().forEach(classInfo -> {
                classFQNList.add(classInfo.getName());
            });
        }
        return classFQNList;
    }

    private ClassInfo getClassInfoByClassFQN(String classFQN) {
        ClassGraph cg = new ClassGraph().enableClassInfo().enableFieldInfo().enableMethodInfo()
            .ignoreClassVisibility().ignoreFieldVisibility().ignoreFieldVisibility();

        for (String classPath : classPathList) {
            cg.overrideClasspath(classPath);
        }
        cg.acceptClasses(classFQN);

        try (ScanResult scanResult = cg.scan()) {
            return scanResult.getClassInfo(classFQN);
        }
    }

    public List<String> getMethodFQNListByClassFQN(String classFQN) {
        ClassInfo classInfoByClassFQN = getClassInfoByClassFQN(classFQN);
        List<String> methodFQNList = new ArrayList<>();
        classInfoByClassFQN.getMethodInfo().forEach(methodInfo -> {
            String methodFQN = getMethodFQN(methodInfo);
            methodFQNList.add(methodFQN);
        });
        return methodFQNList;
    }

    public List<String> getMethodFQNByMethodNameAndClassFQN(String classFQN, String methodName) {
        ClassInfo classInfoByClassFQN = getClassInfoByClassFQN(classFQN);
        List<String> methodFQNList = new ArrayList<>();
        classInfoByClassFQN.getMethodInfo()
            .filter(methodInfo -> methodInfo.getName().equals(methodName))
            .forEach(methodInfo -> {
                String methodFQN = getMethodFQN(methodInfo);
                methodFQNList.add(methodFQN);
            });
        return methodFQNList;
    }

    public List<String> getMethodFQNByMethodNameAndClassFQN(String classFQN, String methodName,
        int paramterCnt) {
        ClassInfo classInfoByClassFQN = getClassInfoByClassFQN(classFQN);
        List<String> methodFQNList = new ArrayList<>();
        classInfoByClassFQN.getMethodInfo()
            .filter(methodInfo -> methodInfo.getName().equals(methodName))
            .filter(methodInfo -> methodInfo.getParameterInfo().length == paramterCnt)
            .forEach(methodInfo -> {
                String methodFQN = getMethodFQN(methodInfo);
                methodFQNList.add(methodFQN);
            });
        return methodFQNList;
    }

    public List<String> getMethodSignatureListByClassFQN(String classFQN) {
        ClassInfo classInfoByClassFQN = getClassInfoByClassFQN(classFQN);
        List<String> methodSignatureList = new ArrayList<>();

        classInfoByClassFQN.getMethodInfo().forEach(methodInfo -> {
            String methodSignature = getDecodedMethodSignature(methodInfo);
            methodSignatureList.add(methodSignature);
        });
        return methodSignatureList;
    }


    private String getDecodedMethodSignature(MethodInfo methodInfo) {
        String methodSignature = methodInfo.getTypeSignatureOrTypeDescriptorStr();
        String methodName = methodInfo.getName();
        System.out.println("coded methodSignature = " + methodSignature);
        String decodedMethodSignature = FQNResolver.decodeMethodSignature(methodSignature,
            methodName);
        System.out.println("decodedMethodSignature = " + decodedMethodSignature);
        return decodedMethodSignature;
    }

    private String getMethodFQN(MethodInfo methodInfo) {
        String className = methodInfo.getClassName();
        String methodName = methodInfo.getName();
        String methodSignature = methodInfo.getTypeSignatureOrTypeDescriptorStr();
        System.out.println("coded methodSignature = " + methodSignature);

        int parameterStartIndex = methodSignature.indexOf("(");
        int parameterEndIndex = methodSignature.indexOf(")");

        String parameters = methodSignature.substring(parameterStartIndex + 1, parameterEndIndex);
        String decodedGenericParams = FQNResolver.decodeGenericSignature(parameters, false);
        String methodFQN = className + "." + methodName + "(" + decodedGenericParams + ")";
        System.out.println("decoded methodFQN = " + methodFQN);
        return methodFQN.replace("$", ".");
    }

}
