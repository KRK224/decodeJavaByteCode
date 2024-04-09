package org.example;

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


public class Main {

    public static void main(String[] args) {
        try {
//            JsonObject jsonObject = new JsonObject();

            ClassPool pool = ClassPool.getDefault();
//            pool.appendClassPath(new LoaderClassPath(classLoader));

            String evictorJarPath = "/Users/krk224/Documents/Tmax/1_source/Projects/evictor-1.0.0.jar";
            String gsonJarPath = "/Users/krk224/.m2/repository/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar";
            String superPxJarPath = "/Users/krk224/Documents/Tmax/1_source/test-px/build/libs/super-px-0.2.2.hotfix1.jar";

//            pool.insertClassPath(evictorJarPath);
//            pool.insertClassPath(gsonJarPath);
            pool.insertClassPath(superPxJarPath);

            /**
             * 현재 ClassLoader에 로드 된 package 목록 출력
             */

//            printPackagesInPool(pool);

            /**
             * 특정 패키지에 속한 클래스들의 FQN 목록을 반환한다.
             */
//            List<String> evictorClassFQNList = getClassFQNListByPackageNameAndJarPath(
//                "com.stoyanr.evictor.map", evictorJarPath);
//            List<String> gsonClassFQNList = getClassFQNListByPackageNameAndJarPath(
//                "com.google.gson", gsonJarPath);
//
//            for (String evictorClassFQN : evictorClassFQNList) {
//                System.out.println("evictorClassFQN = " + evictorClassFQN);
//                printMethod(pool, evictorClassFQN);
//                System.out.println("=====================================");
//            }

//            for (String gsonClassFQN : gsonClassFQNList) {
//                System.out.println("gsonClassFQN = " + gsonClassFQN);
//                printMethod(pool, gsonClassFQN);
//                System.out.println("=====================================");
//            }

            String evictorClass = "com.stoyanr.evictor.map.ConcurrentMapWithTimedEvictionDecorator";
            String gsonClass = "com.google.gson.JsonObject";
            String javaParserClass = "com.github.javaparser.ast.body.ConstructorDeclaration";
            String superappClass = "com.tmax.superobject.servicemanager.ServiceManager";

//            printMethod(pool, evictorClass);
//            System.out.println("=====================================");
            printMethod(pool, gsonClass);
//            System.out.println("=====================================");
//            printMethod(pool, javaParserClass);
//            System.out.println("=====================================");
//            printMethod(pool, superappClass);

            printFieldInClass(pool, superappClass);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printPackagesInPool(ClassPool pool) {
        /**
         * 런타임에 로드된 클래스의 패키지 정보만 얻을 수 있다.
         */
        Package[] definedPackages = pool.getClassLoader().getDefinedPackages();
        for (Package definedPackage : definedPackages) {
            System.out.println("definedPackage = " + definedPackage.getName());
        }
    }


    public static void printFieldInClass(ClassPool pool, String classFullQualifiedName)
        throws Exception {
        CtClass ctClass = pool.get(classFullQualifiedName);
        CtField[] fields = ctClass.getDeclaredFields();

        for (CtField field : fields) {
            System.out.println("++++++++++++++++++++++");
            String fieldType = field.getType().getName();
            String fieldName = field.getName();
            String fieldSignature = field.getSignature();
            String fieldGenericSignature = field.getGenericSignature();

            System.out.println("fieldGenericSignature = " + fieldGenericSignature + ", fieldType = "
                + fieldType + ", fieldName = " + fieldName + ", fieldSignature = "
                + fieldSignature);
            if (fieldGenericSignature != null) {
                System.out.println("decodedGenericField = " +
                    decodeGenericSignature(fieldGenericSignature, false));
            }
            System.out.println("++++++++++++++++++++++");
        }
    }

    public static void printMethod(ClassPool pool, String classFullQualifiedName) throws Exception {
        CtClass ctClass = pool.get(classFullQualifiedName);
        CtMethod[] ctMethods = ctClass.getDeclaredMethods();
        for (CtMethod ctMethod : ctMethods) {
            System.out.println("++++++++++++++++++++++");
            System.out.println(getDecodedMethodSignature(ctMethod));
            System.out.println("ctMethod.getSignature() = " + ctMethod.getSignature());
            System.out.println("ctMethod.getLongName() = " + ctMethod.getLongName());
            System.out.println("ctMethod fqn = " + getMethodFQN(ctClass, ctMethod));
            System.out.println("++++++++++++++++++++++");
        }
    }

    /**
     * 특정 패키지에 속한 클래스들의 FQN 목록을 반환한다.
     *
     * @param packageName
     * @param jarPath
     * @return
     * @throws IOException
     */
    public static List<String> getClassFQNListByPackageNameAndJarPath(String packageName,
        String jarPath)
        throws IOException {
        List<String> classFQNList = new ArrayList<>();
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();

        System.out.println("packageName = " + packageName);
        String entryPackageName = packageName.replace(".", "/");
        System.out.println("entryPackageName = " + entryPackageName);

        /**
         * Jar Entry를 순회하며 package 명을 비교
         */
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith(".class") && entryName.startsWith(entryPackageName)) {
                String classFQN = entryName.replace("/", ".").substring(0, entryName.length() - 6);
                classFQNList.add(classFQN);
            }
        }
        return classFQNList;
    }

    public static String getDecodedMethodSignature(CtMethod ctMethod) {
        String signature = ctMethod.getGenericSignature();

        if (signature == null) {
            signature = ctMethod.getSignature();
        }

        String[] split = signature.split("\\(");
        String typeDef = split[0];
        String[] split2 = split[1].split("\\)");
        String params = split2[0];
        String returnValue = split2[1];

        System.out.println(
            "typeDef = " + typeDef + ", params = " + params + ", returnValue = "
                + returnValue);

        return typeDef.isEmpty() ? decodeGenericSignature(returnValue, false) + " "
            + ctMethod.getName() + "(" + decodeGenericSignature(params, false) + ")" :
            decodeGenericSignature(typeDef, true) + " " + decodeGenericSignature(returnValue,
                false) + " "
                + ctMethod.getName() + "(" + decodeGenericSignature(params, false) + ")";

    }

    public static String getMethodFQN(CtClass ctClass, CtMethod ctMethod) {
        String genericSignature = ctMethod.getGenericSignature();
        if (genericSignature == null) {
            return ctMethod.getLongName().replace(",", ", ").replace("$", ".");
        } else {
            System.out.println("genericSignature = " + genericSignature);
            int paramStartIdx = genericSignature.indexOf("(");
            int paramEndIdx = genericSignature.indexOf(")");
            String params = genericSignature.substring(paramStartIdx + 1, paramEndIdx);
            String decodedGenericParams = decodeGenericSignature(params, false);
            System.out.println("decodedGenericParams = " + decodedGenericParams);
            String methodFQN = ctClass.getName() + "." + ctMethod.getName() + "("
                + decodedGenericParams + ")";
            return methodFQN.replace("$", ".");
        }
    }

    public static String decodeGenericSignature(String genericSignature, boolean isTypeDef) {

        if (genericSignature.isEmpty()) {
            return genericSignature;
        }

        StringBuilder sb = new StringBuilder();
        boolean isArray = false;

        for (int i = 0; i < genericSignature.length(); i++) {
            char c = genericSignature.charAt(i);
            if (c == '[') {
                isArray = true;
            } else if (c == 'L' || c == 'T') {

                // L: Object, T: Generic 시그니처가 타입의 시작을 나타내는 경우
                if (i == 0 || genericSignature.charAt(i - 1) == '['
                    || genericSignature.charAt(i - 1) == ';'
                    || genericSignature.charAt(i - 1) == '<'
                    || genericSignature.charAt(i - 1) == '>'
                    || genericSignature.charAt(i - 1) == '+'
                    || genericSignature.charAt(i - 1) == '-'
                    || genericSignature.charAt(i - 1) == ':'
                    || !BytecodeSignatureEnum.decode(
                        String.valueOf(genericSignature.charAt(i - 1)))
                    .equals(String.valueOf(genericSignature.charAt(i - 1)))
                ) {
                    if (c == 'T' && isTypeDef) {
                        sb.append("T");
                    }

                    for (int j = i + 1; j < genericSignature.length(); j++) {
                        if (genericSignature.charAt(j) == ';' || genericSignature
                            .charAt(j) == '>' || genericSignature.charAt(j) == '<'
                            || genericSignature.charAt(j) == ':') {
                            String objectType = genericSignature.substring(i + 1, j);
                            sb.append(objectType.replace("/", "."));
                            if (isArray) {
                                sb.append("[]");
                                isArray = false;
                            }
                            i = j - 1;
                            break;
                        }
                    }
                }

            } else if (c == ':') {
                sb.append(BytecodeSignatureEnum.decode(String.valueOf(c)));
                if (genericSignature.charAt(i + 1) == ':') {
                    i++;
                }

            } else if (c == ';') {
                if (i == genericSignature.length() - 1
                    || genericSignature.charAt(i + 1) == '>') {
                    continue;
                }
                sb.append(", ");
            } else {
                sb.append(BytecodeSignatureEnum.decode(String.valueOf(c)));

                // BytecodeSignatureEnum에 정의되지 않은 문자인 경우
                if (BytecodeSignatureEnum.decode(String.valueOf(c)).equals(String.valueOf(c))) {
                    continue;
                }

                if (isArray) {
                    sb.append("[]");
                    isArray = false;
                }

                if (i == genericSignature.length() - 1
                    || genericSignature.charAt(i + 1) == '>'
                    || genericSignature.charAt(i + 1) == '<'
                    || genericSignature.charAt(i + 1) == ';') {
                    continue;
                }
                sb.append(", ");
            }
        }

        return sb.toString();

    }

}