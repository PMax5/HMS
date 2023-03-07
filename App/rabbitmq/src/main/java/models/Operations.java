package models;

public enum Operations {
    // Config Service
    CONFIG_REQUEST,

    // Registry Service
    NEW_USER_REQUEST,
    AUTHENTICATION_REQUEST,
    AUTHORIZATION_REQUEST,
    LOGOUT_REQUEST,
    DELETE_USER_REQUEST,

    // Data Service
    SUBMIT_USER_DATALOG,
    GET_USER_DATALOGS,

    // Profiler Service
    REGISTER_PROFILE,
    SET_USER_PROFILE,
    GET_PROFILES
}