package com.cmdlee.quizsushi.admin.repository.projection;

import java.time.LocalDateTime;

public interface StatRawProjection {
    String getLabel();

    LocalDateTime getTime();

    long getCount();
}