package com.github.t1.powerannotations.demo;

import org.eclipse.microprofile.graphql.Query;

import com.github.t1.annotations.MixinFor;

@MixinFor(GreetingBoundary.class)
@SomeAnnotation("mixed-in")
public interface GreetingBoundaryMixin {
	@Query String hello();
}
