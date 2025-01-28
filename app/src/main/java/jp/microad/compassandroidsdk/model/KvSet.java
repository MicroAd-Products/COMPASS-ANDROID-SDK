package jp.microad.compassandroidsdk.model;

import java.util.Objects;

public class KvSet {
    private final String gender;
    private final String birthday;
    private final String age;
    private final String postalCode;
    private final String email;
    private final String hashedEmail;

    public KvSet(String gender, String birthday, String age, String postalCode, String email, String hashedEmail) {
        this.gender = gender;
        this.birthday = birthday;
        this.age = age;
        this.postalCode = postalCode;
        this.email = email;
        this.hashedEmail = hashedEmail;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getAge() {
        return age;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedEmail() {
        return hashedEmail;
    }
}

