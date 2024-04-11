package org.example.fqnresolver;

import org.example.enums.BytecodeSignatureEnum;

public interface FQNResolver {

    static String decodeMethodSignature(String methodGenericSignature, String methodName) {
        String[] split = methodGenericSignature.split("\\(");
        String typeDef = split[0];
        String[] split2 = split[1].split("\\)");
        String params = split2[0];
        String returnValue = split2[1];

        System.out.println(
            "typeDef = " + typeDef + ", params = " + params + ", returnValue = "
                + returnValue);

        return typeDef.isEmpty() ? decodeGenericSignature(returnValue, false) + " "
            + methodName + "(" + decodeGenericSignature(params, false) + ")" :
            decodeGenericSignature(typeDef, true) + " " + decodeGenericSignature(returnValue,
                false) + " "
                + methodName + "(" + decodeGenericSignature(params, false) + ")";
    }

    static String decodeGenericSignature(String genericSignature, boolean isTypeDef) {

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
