package com.github.t1.annotations.impl;

import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.ClassInfo;
import com.github.t1.annotations.index.Index;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

class TypeToMemberResolver {
    private final Index index;

    public TypeToMemberResolver(Index index) { this.index = index; }

    public void resolve() {
        index.allClasses()
            .flatMap(ClassInfo::annotations)
            .forEach(this::resolveToMembers);
    }

    private void resolveToMembers(AnnotationInstance annotationInstance) {
        ClassInfo classInfo = (ClassInfo) annotationInstance.target();
        ClassInfo annotationType = annotationInstance.type();
        if (annotationType.isExplicitlyAllowedOn(FIELD))
            classInfo.fields()
                .filter(field -> field.canBeAdded(annotationType))
                .forEach(field -> field.add(annotationInstance));
        if (annotationType.isExplicitlyAllowedOn(METHOD))
            classInfo.methods()
                .filter(method -> method.canBeAdded(annotationType))
                .forEach(method -> method.add(annotationInstance));
    }
}
