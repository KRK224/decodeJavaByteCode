package org.example.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResolvedStatus {

    private int totalUnQualifiedMethodDeclDTO;
    private int totalUnQualifiedMethodCallExprDTO;
    private int resolvedMethodDeclDTO;
    private int resolvedMethodCallExprDTO;

    @Override
    public String toString() {
        return "ResolvedStatus{" +
            "resolved MethodDeclDTO= " + resolvedMethodDeclDTO + "/" + totalUnQualifiedMethodDeclDTO
            +
            ", resolved MethodCallExprDTO= " + resolvedMethodCallExprDTO + '/'
            + totalUnQualifiedMethodCallExprDTO +
            '}';
    }
}
