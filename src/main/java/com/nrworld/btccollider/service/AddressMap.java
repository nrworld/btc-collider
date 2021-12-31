package com.nrworld.btccollider.service;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class AddressMap {
    private final String address;
    private final Double balance;
}
