package com.devcv.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshTokenResponse {
    private String accessToken;
}
