package org.example.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.experimental.Accessors;


@Builder(toBuilder = true)
public class ClassSignatureInfo {

    @Getter
    private String packageName;
    @Getter
    private String className;
    @Getter
    private String classFQN;
    @Getter
    private String superClassFQN;
    @Getter
    private List<String> interfaceFQNs;
    @Getter
    private String typeDef;
    @Accessors(fluent = true)
    @Getter
    private boolean hasTypeParameter;

    @Default
    private Map<String, TypeDefSignature> typeDefMapByTypeParameter = new HashMap<>();


    @Default
    private Map<String, MethodSignatureInfo> methodSignatureInfoMapByMethodFQN = new HashMap<>();
    @Default
    private Map<String, FieldSignatureInfo> fieldSignatureInfoMapByFieldFQN = new HashMap<>(); // FieldFQN = ClassFQN + '.' + FieldName


    public boolean isAllowPrimitiveType(String type) {
        String className = this.getClassName();
        if (className.equals("Integer") || className.equals("Short") || className.equals("Byte")) {
            return List.of("int", "short", "byte").contains(type);
        } else if (className.equals("Long")) {
            return Objects.equals("long", type);
        } else if (className.equals("Float")) {
            return Objects.equals("float", type);
        } else if (className.equals("Double")) {
            return Objects.equals("double", type);
        } else if (className.equals("Character")) {
            return List.of("char", "int").contains(type);
        } else if (className.equals("Boolean")) {
            return Objects.equals("boolean", type);
        } else {
            return false;
        }
    }

    public TypeDefSignature getTypeDefSignature(String typeParameter) {
        return this.typeDefMapByTypeParameter.get(typeParameter);
    }

    public void addTypeDefSignature(TypeDefSignature typeDefSignature) {
        this.typeDefMapByTypeParameter.put(typeDefSignature.getTypeParameter(), typeDefSignature);
    }

    public List<String> getTypeParameterList() {
        return List.copyOf(this.typeDefMapByTypeParameter.keySet());
    }

    public void addMethodSignatureInfo(String methodFQN, MethodSignatureInfo methodSignatureInfo) {
        this.methodSignatureInfoMapByMethodFQN.put(methodFQN, methodSignatureInfo);
    }

    public MethodSignatureInfo getMethodSignatureInfo(String methodFQN) {
        return this.methodSignatureInfoMapByMethodFQN.get(methodFQN);
    }

    public void addFieldSignatureInfo(FieldSignatureInfo fieldSignatureInfo) {
        this.fieldSignatureInfoMapByFieldFQN.put(fieldSignatureInfo.getMySignature(),
            fieldSignatureInfo);
    }

    public FieldSignatureInfo getFieldSignatureInfo(String fieldFQN) {
        return this.fieldSignatureInfoMapByFieldFQN.get(fieldFQN);
    }


    @Override
    public String toString() {
        return "ClassSignatureInfo{" +
            "packageName='" + packageName + '\'' +
            ", className='" + className + '\'' +
            ", classFQN='" + classFQN + '\'' +
            ", superClassFQN='" + superClassFQN + '\'' +
            ", typeDef='" + typeDef + '\'' +
            '}';
    }
}
