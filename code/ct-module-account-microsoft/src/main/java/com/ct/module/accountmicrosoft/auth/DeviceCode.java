package com.ct.module.accountmicrosoft.auth;

public record DeviceCode(String code, int interval, int expiresIn) {
}
