package org.example.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.experimental.Accessors;


@Builder(toBuilder = true)
public class MethodSignatureInfo {

    @Getter
    private String methodName;
    // 메소드 자체에 type parameter가 있는 경우, 소속된 class의 type parameter와는 무관하다.
    @Getter
    private String typeDef;
    @Accessors(fluent = true)
    @Getter
    private boolean hasTypeParameter;
    @Getter
    private String returnType;
    @Getter
    private String params;
    @Getter
    private List<String> paramTypeList;
    private String methodSignature; // methodByteCodeSignature 원본
    @Getter
    private String belongedClassFQN;

    @Default
    private Map<String, TypeDefSignature> typeDefMapByTypeParameter = new HashMap<>();

    public void addTypeDefSignature(TypeDefSignature typeDefSignature) {
        this.typeDefMapByTypeParameter.put(typeDefSignature.getTypeParameter(), typeDefSignature);
    }

    public TypeDefSignature getTypeDefSignature(String typeParameter) {
        return this.typeDefMapByTypeParameter.get(typeParameter);
    }

    public List<String> getTypeParameters() {
        return List.copyOf(typeDefMapByTypeParameter.keySet());
    }

    public String getMySignature() {
        return belongedClassFQN + "." + methodName + "(" + params + ")";
    }

    @Override
    public String toString() {
        return "MethodSignatureInfo{" +
            "methodName='" + methodName + '\'' +
            ", typeDef='" + typeDef + '\'' +
            ", returnType='" + returnType + '\'' +
            ", params='" + params + '\'' +
            ", methodSignature='" + methodSignature + '\'' +
            '}';
    }

}
