package org.example.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class FieldSignatureInfo {

    private String belongedClassFQN;
    private String fieldName;
    private String fieldTypeFQN; // 선언된 Type의 FQN

    public String getMySignature() {
        return belongedClassFQN + "." + fieldName;
    }

    @Override
    public String toString() {
        return "FieldSignatureInfo{" +
            "belongedClassFQN='" + belongedClassFQN + '\'' +
            "fieldName='" + fieldName + '\'' +
            ", fieldTypeSignature='" + fieldTypeFQN + '\'' +
            '}';
    }

}
