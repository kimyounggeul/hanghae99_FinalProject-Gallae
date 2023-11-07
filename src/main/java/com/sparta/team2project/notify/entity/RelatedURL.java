package com.sparta.team2project.notify.entity;

import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor
public class RelatedURL {

    @Column (nullable = false, length = 500)
    private String url;

    public RelatedURL(String url) {
        if (isNotValidRelatedURL(url)) {
            throw new CustomException(ErrorCode.VALID_NOT_URL);
        }
        this.url=url;
    }

    private boolean isNotValidRelatedURL(String url) {
        return Objects.isNull(url) || url.length() > 500 || url.isEmpty();
    }

    @Override
    public String toString() {
        return url;
    }
}
