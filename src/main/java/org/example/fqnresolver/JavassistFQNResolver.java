package org.example.fqnresolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class JavassistFQNResolver implements FQNResolver {

    private ClassPool pool = ClassPool.getDefault();

    public JavassistFQNResolver(List<String> classpath) throws NotFoundException {
        // Add classpath to the classpool
        for (String path : classpath) {
            pool.insertClassPath(path);
        }
    }

    public List<String> getClassFQNListByJarPath(String jarPath) {
        List<String> classFQNList = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    String classFQN = entryName.replace("/", ".")
                        .substring(0, entryName.length() - 6);
                    classFQNList.add(classFQN);
                    System.out.println("classFQN = " + classFQN);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classFQNList;
    }

    public List<String> getClassFQNListByPackageNameAndJarPath(String packageName, String jarPath) {
        List<String> classFQNList = new ArrayList<>();

        try {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();

            String entryPackageName = packageName.replace(".", "/");

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.endsWith(".class") && entryName.startsWith(entryPackageName)) {
                    String classFQN = entryName.replace("/", ".")
                        .substring(0, entryName.length() - 6);
                    classFQNList.add(classFQN);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classFQNList;
    }

    public List<String> getFieldFQNListByClassFQN(String classFQN) {
        List<String> fieldFQNList = new ArrayList<>();
        try {
            CtClass ctClass = pool.get(classFQN);
            CtField[] declaredFields = ctClass.getDeclaredFields();
            for (CtField field : declaredFields) {
                System.out.println("++++++++++++++++++++++");
                String fieldType = field.getType().getName();
                String fieldName = field.getName();
                String fieldSignature = field.getSignature();
                String fieldGenericSignature = field.getGenericSignature();

                System.out.println(
                    "fieldType = " + fieldType + ", fieldName = " + fieldName + "fieldSignature = "
                        + fieldSignature + ", fieldGenericSignature = " + fieldGenericSignature
                        + ", fieldGenericSignature = " + fieldGenericSignature);
                if (fieldGenericSignature != null) {
                    String decodedFieldSignature = FQNResolver
                        .decodeGenericSignature(fieldGenericSignature, false);
                    fieldFQNList.add(decodedFieldSignature + " " + fieldName);
                    System.out.println("decodedFieldSignature = " + decodedFieldSignature);
                } else {
                    fieldFQNList.add(fieldSignature);
                }
                System.out.println("++++++++++++++++++++++");
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return fieldFQNList;
    }

    public List<String> getMethodSignatureListByClassFQN(String classFQN) {
        List<String> methodSignatureList = new ArrayList<>();
        try {
            CtClass ctClass = pool.get(classFQN);
            CtMethod[] ctMethods = ctClass.getDeclaredMethods();

            for (CtMethod ctMethod : ctMethods) {
                String methodSignature = getDecodedMethodSignature(ctMethod);
                methodSignatureList.add(methodSignature);
            }

        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return methodSignatureList;
    }


    public List<String> getMethodFQNListByClassFQN(String classFQN) {
        List<String> methodFQNList = new ArrayList<>();
        try {
            CtClass ctClass = pool.get(classFQN);
            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : ctMethods) {
                String methodFQN = getMethodFQN(ctMethod, ctClass);
                methodFQNList.add(methodFQN);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return methodFQNList;
    }

    private String getDecodedMethodSignature(CtMethod ctMethod) {
        String methodSignature = ctMethod.getGenericSignature();

        if (methodSignature == null) {
            methodSignature = ctMethod.getSignature();
        }
        String methodName = ctMethod.getName();
        return FQNResolver.decodeMethodSignature(methodSignature, methodName);


    }

    private String getMethodFQN(CtMethod ctMethod, CtClass ctClass) {
        String genericSignature = ctMethod.getGenericSignature();
        if (genericSignature == null) {
            return ctMethod.getLongName().replace(",", ", ").replace("$", ".");
        } else {
            System.out.println("genericSignature = " + genericSignature);
            int paramStartIdx = genericSignature.indexOf("(");
            int paramEndIdx = genericSignature.indexOf(")");
            String params = genericSignature.substring(paramStartIdx + 1, paramEndIdx);
            String decodedGenericParams = FQNResolver.decodeGenericSignature(params, false);
            System.out.println("decodedGenericParams = " + decodedGenericParams);
            String methodFQN = ctClass.getName() + "." + ctMethod.getName() + "("
                + decodedGenericParams + ")";
            return methodFQN.replace("$", ".");
        }
    }


}
