package com.example.netgsm.model;

import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

@Entry(objectClasses = {"inetOrgPerson"}, base = "ou=users")
@Data
public class User {
    @Id
    private String number;

    @Attribute(name = "cn")
    private String name;
}
