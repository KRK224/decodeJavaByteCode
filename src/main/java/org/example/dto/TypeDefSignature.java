package org.example.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class TypeDefSignature {

    private String typeParameter;  // T, D
    private String typeParameterValue;  // 실제 들어온 값도 저장 e.g. java.lang.String, java.lang.Integer
    private String boundClassFQN;  // extends ~
    private List<String> boundInterfaceFQNList;  // implements ~

    @Override
    public String toString() {
        return "TypeDefSignature{" +
            "typeParameter='" + typeParameter + '\'' +
            ", boundClassFQN='" + boundClassFQN + '\'' +
            '}';
    }
}
