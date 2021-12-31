package com.nrworld.btccollider.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class BtcKey {
    @JsonProperty("fromKey")
    private final String fromKey;
    @JsonProperty("privateKeyAsHex")
    private final String privateKeyAsHex;
    @JsonProperty("publicKeyAsHex")
    private final String publicKeyAsHex;
    @JsonProperty("privateKeyAsWiF")
    private final String privateKeyAsWiF;

}
