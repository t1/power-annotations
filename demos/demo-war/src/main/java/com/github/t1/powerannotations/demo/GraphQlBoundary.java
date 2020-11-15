package com.github.t1.powerannotations.demo;

import org.eclipse.microprofile.graphql.GraphQLApi;

import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Stereotype
@GraphQLApi
@SomeOtherAnnotation("stereotyped")
@Retention(RUNTIME)
public @interface GraphQlBoundary {}
