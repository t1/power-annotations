package com.github.t1.annotations.plugin.tck;

import com.github.t1.annotations.MixinFor;

@MixinFor(TargetClass.class)
@MixedInAnnotation("mixed-in-class")
public class MixinClass {
    @MixedInAnnotation("mixed-in-method")
    public void foo() {}
    @MixedInAnnotation("mixed-in-field")
    public String bar;
}
