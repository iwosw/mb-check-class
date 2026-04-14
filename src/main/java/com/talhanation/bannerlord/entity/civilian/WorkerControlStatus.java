package com.talhanation.bannerlord.entity.civilian;

import java.util.Objects;

public class WorkerControlStatus {

    public enum Kind {
        BLOCKED,
        IDLE
    }

    private Kind kind;
    private String reasonToken;

    public boolean shouldNotify(Kind nextKind, String nextReasonToken) {
        if (nextKind == null || nextReasonToken == null || nextReasonToken.isBlank()) {
            return false;
        }

        if (kind == nextKind && Objects.equals(reasonToken, nextReasonToken)) {
            return false;
        }

        kind = nextKind;
        reasonToken = nextReasonToken;
        return true;
    }

    public void clear() {
        kind = null;
        reasonToken = null;
    }

    public Kind getKind() {
        return this.kind;
    }

    public String getReasonToken() {
        return this.reasonToken;
    }
}
