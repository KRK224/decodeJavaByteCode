package org.example.fqnresolver;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import java.util.ArrayList;
import java.util.List;

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

    public ClassInfo getClassInfoByClassFQN(String classFQN) {
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


    private String getDecodedMethodSignature(MethodInfo methodInfo) {
        String methodSignature = methodInfo.getTypeSignatureOrTypeDescriptorStr();
        String methodName = methodInfo.getName();

        return FQNResolver.decodeMethodSignature(methodSignature, methodName);
    }

    private String getMethodFQN(MethodInfo methodInfo) {
        String className = methodInfo.getClassName();
        String methodName = methodInfo.getName();
        String methodSignature = methodInfo.getTypeSignatureOrTypeDescriptorStr();

        int parameterStartIndex = methodSignature.indexOf("(");
        int parameterEndIndex = methodSignature.indexOf(")");

        String parameters = methodSignature.substring(parameterStartIndex + 1, parameterEndIndex);
        String decodedGenericParams = FQNResolver.decodeGenericSignature(parameters, false);
        String methodFQN = className + "." + methodName + "(" + decodedGenericParams + ")";
        return methodFQN.replace("$", ".");
    }

}
