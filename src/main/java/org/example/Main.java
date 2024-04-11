package org.example;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
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
import org.example.enums.BytecodeSignatureEnum;
import org.example.fqnresolver.ClassGraphFQNResolver;


public class Main {

    public static void main(String[] args) {
        try {

            List<String> classPathList = new ArrayList<>();

            String evictorJarPath = "/Users/krk224/Documents/Tmax/1_source/Projects/evictor-1.0.0.jar";
            String gsonJarPath = "/Users/krk224/.m2/repository/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar";
            String superPxJarPath = "/Users/krk224/Documents/Tmax/1_source/test-px/build/libs/super-px-0.2.2.hotfix1.jar";
            classPathList.add(evictorJarPath);
            classPathList.add(gsonJarPath);
            classPathList.add(superPxJarPath);

            ClassGraphFQNResolver classGraphFQNResolver = new ClassGraphFQNResolver(classPathList);

            List<String> classFQNListByPackageName = classGraphFQNResolver.getClassFQNListByPackageName(
                "com.google.gson");

            for (String classFQN : classFQNListByPackageName) {
                System.out.println("classFQN = " + classFQN);
                List<String> methodFQNListByClassFQN = classGraphFQNResolver.getMethodFQNListByClassFQN(
                    classFQN);
                List<String> methodSignatureListByClassFQN = classGraphFQNResolver.getMethodSignatureListByClassFQN(
                    classFQN);
                for (String methodFQN : methodFQNListByClassFQN) {
                    System.out.println("methodFQN = " + methodFQN);
                }
                
            }

            String evictorClass = "com.stoyanr.evictor.map.ConcurrentMapWithTimedEvictionDecorator";
            String gsonClass = "com.google.gson.JsonObject";
            String javaParserClass = "com.github.javaparser.ast.body.ConstructorDeclaration";
            String superappClass = "com.tmax.superobject.servicemanager.ServiceManager";

//            List<String> methodFQNListByClassFQN = classGraphFQNResolver.getMethodFQNListByClassFQN(
//                evictorClass);
//            for (String methodFQN : methodFQNListByClassFQN) {
//                System.out.println("methodFQN = " + methodFQN);
//            }

//            List<String> methodSignatureListByClassFQN = classGraphFQNResolver.getMethodSignatureListByClassFQN(
//                javaParserClass);
//            for (String methodSignature : methodSignatureListByClassFQN) {
//                System.out.println("methodSignature = " + methodSignature);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}