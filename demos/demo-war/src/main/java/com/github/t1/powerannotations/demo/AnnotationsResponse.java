package com.github.t1.powerannotations.demo;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AnnotationsResponse {
    @Id
    private String id;
    private String mixedInValue;
    private String stereotypedValue;

    public String getId() { return id; }

    public AnnotationsResponse setId(String id) { this.id = id; return this; }

    public String getMixedInValue() { return mixedInValue; }

    public AnnotationsResponse setMixedInAnnotation(String mixedInValue) { this.mixedInValue = mixedInValue; return this; }

    public String getStereotypedValue() { return stereotypedValue; }

    public AnnotationsResponse setStereotypedAnnotation(String stereotypedValue) { this.stereotypedValue = stereotypedValue; return this; }
}
