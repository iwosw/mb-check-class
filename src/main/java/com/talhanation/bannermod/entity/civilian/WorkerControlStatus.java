package com.talhanation.bannermod.entity.civilian;

import java.util.Objects;

public class WorkerControlStatus {

    public enum Kind {
        BLOCKED,
        IDLE
    }

    private Kind kind;
    private String reasonToken;
    private String reasonMessage;

    public boolean shouldNotify(Kind nextKind, String nextReasonToken, String nextReasonMessage) {
        if (nextKind == null || nextReasonToken == null || nextReasonToken.isBlank()) {
            return false;
        }

        if (kind == nextKind
                && Objects.equals(reasonToken, nextReasonToken)
                && Objects.equals(reasonMessage, nextReasonMessage)) {
            return false;
        }

        kind = nextKind;
        reasonToken = nextReasonToken;
        reasonMessage = nextReasonMessage;
        return true;
    }

    public void clear() {
        kind = null;
        reasonToken = null;
        reasonMessage = null;
    }

    public Kind kind() {
        return kind;
    }

    public String reasonToken() {
        return reasonToken;
    }

    public String reasonMessage() {
        return reasonMessage;
    }
}
