package com.github.t1.annotations.impl;

import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.ClassInfo;
import com.github.t1.annotations.index.Index;
import com.github.t1.annotations.index.MethodInfo;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class InheritedResolver {
    private final Index index;

    public InheritedResolver(Index index) { this.index = index; }

    public void resolve() {
        index.allClasses().forEach(this::resolveFromSuperTypes);
    }

    private void resolveFromSuperTypes(ClassInfo classInfo) {
        List<AnnotationInstance> annotations = classInfo.typeTree()
            .flatMap(ClassInfo::annotations)
            .distinct() // can already have been added to a super type
            .collect(toList());
        classInfo.replaceAnnotations(annotations);

        classInfo.methods().forEach(this::resolveFromSuperTypes);
    }

    private void resolveFromSuperTypes(MethodInfo methodInfo) {
        List<AnnotationInstance> annotations = methodInfo.declaringClass().typeTree()
            .flatMap(classInfo -> classInfo.findMethod(methodInfo.signature()))
            .flatMap(MethodInfo::annotations)
            .distinct() // can already have been added to a super type
            .collect(toList());
        methodInfo.replaceAnnotations(annotations);
    }
}
