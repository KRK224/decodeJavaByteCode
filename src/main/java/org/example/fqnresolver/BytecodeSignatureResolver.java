package org.example.fqnresolver;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.dto.ClassSignatureInfo;
import org.example.dto.FieldSignatureInfo;
import org.example.dto.MethodSignatureInfo;
import org.example.dto.TypeDefSignature;
import org.example.enums.BytecodeSignatureEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface BytecodeSignatureResolver<T> {

    static final Logger logger = LoggerFactory.getLogger(BytecodeSignatureResolver.class);

    public abstract boolean resolve(T t) throws Exception;


    public static List<String> primitiveTypeOrKeywordList = new ArrayList<>(
            Arrays.asList("boolean", "byte", "char",
                    "short", "int", "long", "float", "double", "void", "super", "extends"));

    /**
     * '<>' 의 끝 index를 반환
     *
     * @param genericSignature : <T:Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;>
     * @return : 38
     */
    public static int findLastGenericIndex(String genericSignature) {
        if (!genericSignature.startsWith("<")) {
            return -1;
        }
        int count = 0;
        for (int i = 0; i < genericSignature.length(); i++) {
            char c = genericSignature.charAt(i);
            if (c == '<') {
                count++;
            } else if (c == '>') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    // Todo. SignatureInfo를 추출하는 InnerClass를 만들어서 분리할 것.
    static ClassSignatureInfo getClassSignatureInfo(ClassInfo classInfo) throws Exception {
        String packageName = classInfo.getPackageName();
        String className = classInfo.getSimpleName();
        String classFQN = classInfo.getName();  // Type Parameter가 없는 FQN

        String classBytecodeSignature = classInfo.getTypeSignatureStr();

        if (classBytecodeSignature == null) {
            return ClassSignatureInfo.builder().classFQN(classFQN).packageName(packageName)
                    .className(className).build();
        }
        String typeDef = "";
        String superClass = "";
        List<String> interfaces = new ArrayList<>();

        int lastGenericIndex = findLastGenericIndex(classBytecodeSignature);

        if (lastGenericIndex != -1) {
            typeDef = classBytecodeSignature.substring(0, lastGenericIndex + 1);
            classBytecodeSignature = classBytecodeSignature.substring(lastGenericIndex + 1);
        }

        boolean passedFirstSemiColon = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < classBytecodeSignature.length(); i++) {
            char c = classBytecodeSignature.charAt(i);
            if (c == '<') {
                int lastGenericIdx = findLastGenericIndex(classBytecodeSignature.substring(i));
                if (lastGenericIdx != -1) {
                    String genericStr = classBytecodeSignature.substring(i, i + lastGenericIdx + 1);
                    sb.append(genericStr);
                    i += lastGenericIdx;
                } else {
                    throw new Exception("Failed to find last generic index");
                }
            } else {
                sb.append(c);
                if (c == ';') {
                    if (passedFirstSemiColon) {
                        interfaces.add(sb.toString());
                        sb = new StringBuilder();
                    } else {
                        superClass = sb.toString();
                        sb = new StringBuilder();
                        passedFirstSemiColon = true;
                    }
                }
            }
        }

        boolean hasTypeDef = !typeDef.isEmpty();
        String decodedTypeDef = null;
        Map<String, TypeDefSignature> typeDefSignatureMap = new HashMap<>();
        if (hasTypeDef) {
            decodedTypeDef = decodeTypeDef(typeDef);
            typeDefSignatureMap = getTypeDefSignatureMap(
                    decodedTypeDef);
        }

        String decodedSuperClass = decodeBytecodeSignature(superClass);
        List<String> decodedInterfaces = new ArrayList<>();

        for (String interfaceStr : interfaces) {
            decodedInterfaces.add(decodeBytecodeSignature(interfaceStr));
        }

        // Method와 Field Signature는 공백으로 리턴
        return ClassSignatureInfo.builder().classFQN(classFQN).packageName(packageName)
                .className(className).typeDef(decodedTypeDef).superClassFQN(decodedSuperClass)
                .interfaceFQNs(decodedInterfaces).typeDefMapByTypeParameter(typeDefSignatureMap)
                .build();
    }


    /**
     * @param methodInfo
     * @return
     */
    static MethodSignatureInfo getMethodSignatureInfo(MethodInfo methodInfo) {
        String methodBytecodeSignature = methodInfo.getTypeSignatureOrTypeDescriptorStr();
        String methodName = methodInfo.getName();
        String belongedClassFQN = methodInfo.getClassName();

        String[] split = methodBytecodeSignature.split("\\(");
        String typeDef = split[0];
        String[] split2 = split[1].split("\\)");
        String params = split2[0];
        String returnValue = split2[1];

        boolean hasTypeDef = !typeDef.isEmpty();
        String decodedTypeDef = null;
        Map<String, TypeDefSignature> typeDefSignatureMap = new HashMap<>();

        if (hasTypeDef) {
            decodedTypeDef = decodeTypeDef(typeDef);
            typeDefSignatureMap = getTypeDefSignatureMap(decodedTypeDef);
        }

        String decodedParams = decodeBytecodeSignature(params);
        List<String> paramTypeList = splitSequenceOfTypeByComma(decodedParams);
        String decodedReturnValue = decodeBytecodeSignature(returnValue);

//        logger.debug("typeDef = {}, params = {}, returnValue = {}", decodedTypeDef, decodedParams,
//            decodedReturnValue);

        return MethodSignatureInfo.builder().hasTypeParameter(hasTypeDef).typeDef(decodedTypeDef)
                .methodName(methodName)
                .params(decodedParams).returnType(decodedReturnValue)
                .methodSignature(methodBytecodeSignature).belongedClassFQN(belongedClassFQN)
                .paramTypeList(paramTypeList)
                .hasTypeParameter(hasTypeDef).typeDefMapByTypeParameter(typeDefSignatureMap)
                .build();

    }

    static String getMethodFQN(MethodInfo methodInfo) {
        String className = methodInfo.getClassName();
        String methodName = methodInfo.getName();
        String methodSignature = methodInfo.getTypeSignatureOrTypeDescriptorStr();

        int parameterStartIdx = methodSignature.indexOf("(");
        int parameterEndIdx = methodSignature.indexOf(")");

        String parameters = methodSignature.substring(parameterStartIdx + 1, parameterEndIdx);
        String decodedParameters = BytecodeSignatureResolver.decodeBytecodeSignature(parameters);
        String methodFQN = className + "." + methodName + "(" + decodedParameters + ")";
//        logger.debug("decoded methodFQN = {}", methodFQN);
        return methodFQN;
    }

    static FieldSignatureInfo getFieldSignatureInfo(FieldInfo fieldInfo) {
        String fieldBytecodeSignature = fieldInfo.getTypeSignatureOrTypeDescriptorStr();
        String fieldTypeSignature = decodeBytecodeSignature(fieldBytecodeSignature);
        String fieldName = fieldInfo.getName();
        String belongedClassFQN = fieldInfo.getClassName();

        return FieldSignatureInfo.builder().fieldTypeFQN(fieldTypeSignature)
                .fieldName(fieldName)
                .belongedClassFQN(belongedClassFQN).build();
    }

    static List<String> splitSequenceOfTypeByComma(String genericType) {
        List<String> genericTypeList = new ArrayList<>();

        if (genericType.isEmpty()) {
            return genericTypeList;
        }

        int start = 0;
        int end = 0;
        while (end < genericType.length()) {
            if (genericType.charAt(end) == ',') {
                genericTypeList.add(genericType.substring(start, end).trim());
                start = ++end;
            } else if (genericType.charAt(end) == '<') {
                int lastIndexOfGeneric = BytecodeSignatureResolver.findLastGenericIndex(
                        genericType.substring(end));
                if (lastIndexOfGeneric == -1) {
                    break;
                }
                end = end + lastIndexOfGeneric + 1;
            } else {
                end++;
            }
        }

        genericTypeList.add(genericType.substring(start, end).trim());
        return genericTypeList;
    }

    private static String decodeBytecodeSignature(String bytecodeSignature) {

        if (bytecodeSignature.isEmpty()) {
            return bytecodeSignature;
        }

        StringBuilder sb = new StringBuilder();
        boolean isArray = false;

        for (int i = 0; i < bytecodeSignature.length(); i++) {
            char c = bytecodeSignature.charAt(i);
            if (c == '[') {
                isArray = true;
            } else if (c == 'L' || c == 'T' || c == '.') {

                // 'L': Object, 'T': Generic 시그니처가 타입의 시작을 나타내는 경우
                // '.': 내부 클래스를 호출하는 외부 클래스에 Generic이 존재해서 '.'으로 시작 경우 (ex. OuterClass<T>.InnerClass)
                if (i == 0 || bytecodeSignature.charAt(i - 1) == '['
                        || bytecodeSignature.charAt(i - 1) == ';'
                        || bytecodeSignature.charAt(i - 1) == '<'
                        || bytecodeSignature.charAt(i - 1) == '>'
                        || bytecodeSignature.charAt(i - 1) == '+'
                        || bytecodeSignature.charAt(i - 1) == '-'
                        || bytecodeSignature.charAt(i - 1) == ':'
                        // 직전이 BytecodeSignatureEnum에 정의된 문자인 경우(변환됨) L과 T가 시작임.
                        || !BytecodeSignatureEnum.decode(
                                String.valueOf(bytecodeSignature.charAt(i - 1)))
                        .equals(String.valueOf(bytecodeSignature.charAt(i - 1)))
                ) {
                    // T, L의 경우 생략, '.'의 경우는 추가
                    sb.append(BytecodeSignatureEnum.decode(String.valueOf(c)));

                    // TypeDef는 따로 메소드 분리.
//                    if ((c == 'T' && isTypeDef) || c == '.') {
//                        sb.append(c);
//                    }

                    for (int j = i + 1; j < bytecodeSignature.length(); j++) {
                        if (bytecodeSignature.charAt(j) == ';' || bytecodeSignature
                                .charAt(j) == '>' || bytecodeSignature.charAt(j) == '<'
                                || bytecodeSignature.charAt(j) == ':') {
                            String objectType = bytecodeSignature.substring(i + 1, j);
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

            } else if (c == ':' || c == '+' || c == '-') {
                sb.append(BytecodeSignatureEnum.decode(String.valueOf(c)));
                if (bytecodeSignature.charAt(i + 1) == ':') {
                    i++;
                }

            } else if (c == ';') {
                if (i == bytecodeSignature.length() - 1
                        || bytecodeSignature.charAt(i + 1) == '>') {
                    continue;
                }
                // 인터페이스 다중 상속
                if (bytecodeSignature.charAt(i + 1) == ':') {
                    sb.append(" & ");
                    i++;
                } else {
                    sb.append(", ");
                }

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

                if (i == bytecodeSignature.length() - 1
                        || bytecodeSignature.charAt(i + 1) == '>'
                        || bytecodeSignature.charAt(i + 1) == '<'
                        || bytecodeSignature.charAt(i + 1) == ';') {
                    continue;
                }
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    /**
     * ClassInfo의 bytecode에서 typeDef bytecode가 존재할 때 이 부분만을 전달함.
     * <p>
     * class의 typeSignaureStr : <T:Ljava/lang/Object;>Ljava/lang/Object;
     * </p>
     *
     * @param typeDef : bytecode 내부의 typeDef 부분 <T:Ljava/lang/Object;>
     * @return <T extends java.lang.Object>
     */
    static String decodeTypeDef(String typeDef) {
        if (typeDef.isEmpty()) {
            return typeDef;
        }
        StringBuilder sb = new StringBuilder();

        int previousColonIndex = -1;
        int colonIndex = 0;
        String typeChunk;
        String decodedTypeChunk;

        while (true) {
            colonIndex = typeDef.indexOf(":", colonIndex + 1);
            if (colonIndex == -1) {
                break;
            }
            if (typeDef.charAt(colonIndex - 1) == ';') {
                colonIndex++;
            } else {
                if (previousColonIndex != -1) {
                    typeChunk = typeDef.substring(previousColonIndex, colonIndex - 1);
                    decodedTypeChunk = decodeBytecodeSignature(typeChunk);
                    sb.append(decodedTypeChunk + ", " + typeDef.charAt(colonIndex - 1));
                } else {
                    sb.append(typeDef.substring(0, colonIndex));
                }
                previousColonIndex = colonIndex;
                // :: -> skip next :
                if (typeDef.charAt(colonIndex + 1) == ':') {
                    colonIndex++;
                }
            }
        }

        typeChunk = typeDef.substring(previousColonIndex);
        decodedTypeChunk = decodeBytecodeSignature(typeChunk);
        sb.append(decodedTypeChunk);

        return sb.toString();
    }

    /**
     * type parameter를 key로 하는 TypeDefSignature 맵 반환.
     * <p>
     * <T extends java.lang.Object> -> T: Object
     *
     * @param decodedTypeDef : decodeTypeDef 결과
     * @return
     */
    static Map<String, TypeDefSignature> getTypeDefSignatureMap(String decodedTypeDef) {
        if (decodedTypeDef == null || decodedTypeDef.isEmpty()) {
            return null;
        }
        Map<String, TypeDefSignature> typeDefMapByTypeParameter = new HashMap<>();
        // '<', '>' 제거
        String contents = decodedTypeDef.substring(1, decodedTypeDef.length() - 1);
        String[] split = contents.split(" extends ");
        String typeParameter = split[0].trim();

        for (int i = 1; i < split.length; i++) {
            if (i == split.length - 1) {
                // 마지막
                TypeDefSignature typeDefSignature = createTypeDefSignature(typeParameter, split[i]);
                typeDefMapByTypeParameter.put(typeParameter, typeDefSignature);
            } else {
                // 중간
                int findLastComma = split[i].lastIndexOf(",");
                String boundClassInfo = split[i].substring(0, findLastComma).trim();
                TypeDefSignature typeDefSignature = createTypeDefSignature(typeParameter,
                        boundClassInfo);
                typeDefMapByTypeParameter.put(typeParameter, typeDefSignature);
                typeParameter = split[i].substring(findLastComma + 1).trim();
            }
        }
        return typeDefMapByTypeParameter;
    }

    private static TypeDefSignature createTypeDefSignature(String typeParameter,
                                                           String boundClassInfo) {
        if (boundClassInfo.contains("&")) {
            // 인터페이스 다중 상속
            String[] interfaces = boundClassInfo.split("&");
            String boundClassFQN = interfaces[0].trim();
            List<String> interfaceList = new ArrayList<>();
            for (int i = 1; i < interfaces.length; i++) {
                interfaceList.add(interfaces[i].trim());
            }
            return TypeDefSignature.builder().typeParameter(typeParameter)
                    .boundClassFQN(boundClassFQN).boundInterfaceFQNList(interfaceList).build();
        } else {
            return TypeDefSignature.builder().typeParameter(typeParameter)
                    .boundClassFQN(boundClassInfo.trim())
                    .build();
        }
    }

    // 모든 타입(제네릭 포함)을 resolve하는 함수
    // 안에서 일반 타입을 resolve하는 resolveTypeFQN 호출함.

    /**
     * 모든 타입(제네릭 포함)을 resolve하는 함수, 안에서 일반 타입을 resolve하는 resolveTypeFQN 호출함.
     *
     * @param type               : 소스코드에서 작성하는 type, e.g. String[]
     * @param classFQNMapByType: type을 통해 fqn을 가져오는 맵
     * @return e.g. java.lang.String[]
     * @throws Exception
     */
    static String resolveGenericTypeFQN(String type, Map<String, String> classFQNMapByType)
            throws Exception {
        StringBuilder resolvedFQN = new StringBuilder();
        int start = 0;
        int end = 0;

        while (end < type.length()) {
            char c = type.charAt(end);
            if (type.charAt(end) == '<' || type.charAt(end) == '>'
                    || type.charAt(end) == ',' || type.charAt(end) == ' ' || type.charAt(end) == '['
                    || type.charAt(end) == ']') {
                if (start != end) {
                    // 시작과 끝이 다르면 resolve 할 타입이 직전에 존재.
                    String chunkedType = type.substring(start, end);
                    String resolvedFqnChunk = resolveTypeFQN(chunkedType, classFQNMapByType);
                    if (resolvedFqnChunk == null) {
                        throw new Exception("Failed to resolve generic FQN");
                    }
                    resolvedFQN.append(resolvedFqnChunk);
                }
                resolvedFQN.append(c);
                start = ++end;
            } else {
                end++;
            }
        }
        if (start != end) {
            String chunkedType = type.substring(start, end);
            String resolvedFqnChunk = resolveTypeFQN(chunkedType, classFQNMapByType);
            if (resolvedFqnChunk == null) {
                throw new Exception("Failed to resolve generic FQN");
            }
            resolvedFQN.append(resolvedFqnChunk);
        }
        return resolvedFQN.toString();
    }

    private static String resolveTypeFQN(String type,
                                         Map<String, String> classFQNMapByType) {
        StringBuilder resolvedFQN = new StringBuilder();
        String chunkedType = type;

        if (primitiveTypeOrKeywordList.contains(chunkedType)) {
            // primitive Type으로 그대로 반환
            logger.debug("primitive type or keyword: {}", chunkedType);
            return chunkedType;
        }

        if (chunkedType.contains(".")) {
            // innerClass 라고 판단
            String[] split = chunkedType.split("\\.");
            String resolvedFqnChunk = classFQNMapByType.get(split[0]);
            if (resolvedFqnChunk == null) {
                return null;
            } else {
                // innerClass의 경우 $로 구분
                return resolvedFQN.append(resolvedFqnChunk) + "$" + split[1];
            }
        } else {
            String resolvedFqnChunk = classFQNMapByType.get(chunkedType);
            if (resolvedFqnChunk == null) {
                // Object 타입인 경우
                if (chunkedType.equals("Object")) {
                    return resolvedFQN.append("java.lang.Object").toString();
                }
                // 타입 파라미터 or primitive 라고 판단. 일단 그대로 append e.g. T, E
                logger.debug("it might be type parameter : {}", chunkedType);
                return resolvedFQN.append(chunkedType).toString();
            } else {
                return resolvedFQN.append(resolvedFqnChunk).toString();
            }
        }
    }
}
