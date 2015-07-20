package com.hwtxframework.ioc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class ComponentBundle {

    private Object instance;
    private String className;
    private String name;
    private String postConstruct;
    private String preDestroy;
    private Set<String> dependons;
    private Set<Property> properties = new HashSet<>();
}
