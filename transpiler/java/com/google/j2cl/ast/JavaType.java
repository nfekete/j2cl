/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.ast;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.j2cl.ast.processors.Context;
import com.google.j2cl.ast.processors.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

/**
 * A node that represents a Java Type declaration in the compilation unit.
 */
@Visitable
@Context
public class JavaType extends Node {
  /**
   * Describes the kind of the Java type.
   */
  public enum Kind {
    CLASS,
    INTERFACE,
    ENUM
  }

  private Kind kind;
  private Visibility visibility;
  private boolean isStatic;
  private boolean isLocal;
  private boolean isAbstract;
  @Visitable @Nullable TypeDescriptor enclosingTypeDescriptor;
  @Visitable @Nullable TypeDescriptor superTypeDescriptor;
  @Visitable List<TypeDescriptor> superInterfaceTypeDescriptors = new ArrayList<>();
  @Visitable TypeDescriptor typeDescriptor;
  @Visitable List<Field> fields = new ArrayList<>();
  @Visitable List<Method> methods = new ArrayList<>();
  @Visitable List<Block> instanceInitializerBlocks = new ArrayList<>();
  @Visitable List<Block> staticInitializerBlocks = new ArrayList<>();

  // Used to store Types that need to call clinit within this types clinit as a temporary hack.
  // We use a sorted set so that the order is deterministic.
  private SortedSet<TypeDescriptor> staticFieldClinits = new TreeSet<>();

  // Used to store the original native type for a synthesized JsOverlyImpl type.
  private TypeDescriptor nativeTypeDescriptor;

  public JavaType(Kind kind, Visibility visibility, TypeDescriptor typeDescriptor) {
    this.kind = kind;
    this.visibility = visibility;
    this.typeDescriptor = typeDescriptor;
  }


  public final SortedSet<TypeDescriptor> getStaticFieldClinits() {
    return staticFieldClinits;
  }

  public Kind getKind() {
    return kind;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
  }

  public boolean isLocal() {
    return isLocal;
  }

  public void setLocal(boolean isLocal) {
    this.isLocal = isLocal;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public boolean containsNonJsNativeMethods() {
    for (Method method : methods) {
      if (method.isNative()
          && !method.getDescriptor().isJsProperty()
          && !method.getDescriptor().isJsMethod()) {
        return true;
      }
    }
    return false;
  }

  public boolean containsNativeMethods() {
    for (Method method : methods) {
      if (method.isNative()) {
        return true;
      }
    }
    return false;
  }

  public boolean containsJsOverlay() {
    for (Method method : methods) {
      if (method.getDescriptor().isJsOverlay()) {
        return true;
      }
    }
    for (Field field : fields) {
      if (field.getDescriptor().isJsOverlay()) {
        return true;
      }
    }
    return false;
  }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }

  public boolean isEnum() {
    return this.kind == Kind.ENUM;
  }

  public boolean isInterface() {
    return this.kind == Kind.INTERFACE;
  }

  public TypeDescriptor getNativeTypeDescriptor() {
    Preconditions.checkArgument(
        !(nativeTypeDescriptor != null && superTypeDescriptor != null),
        "A JsInterop Overlay type should not have super class.");
    return this.nativeTypeDescriptor;
  }

  public void setNativeTypeDescriptor(TypeDescriptor nativeTypeDescriptor) {
    Preconditions.checkArgument(
        !(nativeTypeDescriptor != null && superTypeDescriptor != null),
        "A JsInterop Overlay type should not have super class.");
    this.nativeTypeDescriptor = nativeTypeDescriptor;
  }

  public boolean isJsOverlayImpl() {
    return getNativeTypeDescriptor() != null;
  }

  public List<Field> getFields() {
    return fields;
  }

  public void addField(Field field) {
    fields.add(field);
  }

  public void addFields(List<Field> fields) {
    Preconditions.checkNotNull(fields);
    this.fields.addAll(fields);
  }

  /**
   * Since enum fields are just tracked as static final fields in JavaType we want to be able to
   * distinguish enum fields from static fields created in the enum body.
   */
  public List<Field> getEnumFields() {
    Preconditions.checkArgument(this.kind == Kind.ENUM);
    Iterable<Field> enumFields =
        Iterables.filter(
            fields,
            new Predicate<Field>() {
              @Override
              public boolean apply(Field field) {
                return field.isEnumField();
              }
            });
    return Lists.newArrayList(enumFields);
  }

  public List<Method> getMethods() {
    return methods;
  }

  public void addMethod(Method method) {
    methods.add(method);
  }

  public void addMethod(int index, Method method) {
    Preconditions.checkArgument(index >= 0 && index <= methods.size());
    methods.add(index, method);
  }

  public void addMethods(List<Method> methods) {
    this.methods.addAll(methods);
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }

  public List<Block> getInstanceInitializerBlocks() {
    return instanceInitializerBlocks;
  }

  public void addInstanceInitializerBlock(Block instanceInitializer) {
    this.instanceInitializerBlocks.add(instanceInitializer);
  }

  public List<Block> getStaticInitializerBlocks() {
    return staticInitializerBlocks;
  }

  public void addStaticInitializerBlock(Block staticInitializer) {
    this.staticInitializerBlocks.add(staticInitializer);
  }

  public TypeDescriptor getEnclosingTypeDescriptor() {
    return enclosingTypeDescriptor;
  }

  public void setEnclosingTypeDescriptor(TypeDescriptor enclosingTypeDescriptor) {
    this.enclosingTypeDescriptor = enclosingTypeDescriptor;
  }

  public TypeDescriptor getSuperTypeDescriptor() {
    Preconditions.checkArgument(
        !(nativeTypeDescriptor != null && superTypeDescriptor != null),
        "A Java type with a SuperClass can't also be a JsInterop Overlay.");
    return superTypeDescriptor;
  }

  public void setSuperTypeDescriptor(TypeDescriptor superTypeDescriptor) {
    Preconditions.checkArgument(
        !(nativeTypeDescriptor != null && superTypeDescriptor != null),
        "A Java type with a SuperClass can't also be a JsInterop Overlay.");
    this.superTypeDescriptor = superTypeDescriptor;
  }

  public List<TypeDescriptor> getSuperInterfaceTypeDescriptors() {
    return superInterfaceTypeDescriptors;
  }

  public void addSuperInterfaceDescriptor(TypeDescriptor superInterfaceTypeDescriptor) {
    this.superInterfaceTypeDescriptors.add(superInterfaceTypeDescriptor);
  }

  public TypeDescriptor getDescriptor() {
    return typeDescriptor;
  }

  public void setDescriptor(TypeDescriptor typeDescriptor) {
    this.typeDescriptor = typeDescriptor;
  }

  public List<Field> getInstanceFields() {
    return Lists.newArrayList(
        Iterables.filter(
            getFields(),
            new Predicate<Field>() {
              @Override
              public boolean apply(Field field) {
                return !field.getDescriptor().isStatic();
              }
            }));
  }

  public List<Field> getStaticFields() {
    return Lists.newArrayList(
        Iterables.filter(
            getFields(),
            new Predicate<Field>() {
              @Override
              public boolean apply(Field field) {
                return field.getDescriptor().isStatic();
              }
            }));
  }

  public List<Method> getConstructors() {
    return Lists.newArrayList(
        Iterables.filter(
            getMethods(),
            new Predicate<Method>() {
              @Override
              public boolean apply(Method method) {
                return method.isConstructor();
              }
            }));
  }

  @Override
  public Node accept(Processor processor) {
    return Visitor_JavaType.visit(processor, this);
  }
}
