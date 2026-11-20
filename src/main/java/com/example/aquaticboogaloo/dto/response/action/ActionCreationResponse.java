package com.example.aquaticboogaloo.dto.response.action;


import com.example.aquaticboogaloo.dto.request.ActionRequest;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ActionCreationResponse {
    List<ActionResponse> createdActions = new ArrayList<>();
    List<FailedValidationActionResponse> failedActionRequests = new ArrayList<>();

    public void add(ActionResponse createdAction) {
        if (createdAction == null) return;
        createdActions.add(createdAction);
    }
    public void add(FailedValidationActionResponse failedAction) {
        if (failedAction == null) return;
        failedActionRequests.add(failedAction);
    }

    public void addFailure(ActionRequest ar, String cause) {
        if (ar == null) return;
        var failed = new FailedValidationActionResponse(ar);
        if (cause != null) failed.addCause(cause);
        failedActionRequests.add(failed);
    }
}
