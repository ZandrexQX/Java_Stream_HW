package ru.gb.lesson5;

public class UsersRequest extends AbstractRequest {

    public static final String TYPE = "getUsers";

    public UsersRequest() {
        setType(TYPE);
    }
}
