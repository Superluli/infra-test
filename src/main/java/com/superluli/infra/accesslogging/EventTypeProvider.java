package com.superluli.infra.accesslogging;

import javax.servlet.http.HttpServletRequest;

public interface EventTypeProvider {

    public String getEventType(HttpServletRequest request);
}
