package com.example.aquaticboogaloo.dto.response;

import com.example.aquaticboogaloo.dto.request.ActionRequest;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FailedValidationActionResponse {
    ActionRequest actionRequest;
    List<String> causeMessages = new ArrayList<>();

    public FailedValidationActionResponse(ActionRequest ar) {
        actionRequest = ar;
    }

    public void addAllCauses(List<String> messages) {
        if (messages == null || messages.isEmpty()) return;
        messages.forEach(this::addCause);
    }
    public void addCause(String message) {
        if (message == null) return;
        causeMessages.add(message);
    }

    public boolean isEmpty() {
        return causeMessages.isEmpty();
    }
}
