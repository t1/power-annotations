package com.github.t1.powerannotations.demo;

import com.github.t1.annotations.MixinFor;

import javax.persistence.Id;

@MixinFor(Id.class)
@org.eclipse.microprofile.graphql.Id
public @interface IdMixin {
}
