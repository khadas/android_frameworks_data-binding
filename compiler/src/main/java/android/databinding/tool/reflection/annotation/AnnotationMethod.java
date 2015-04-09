/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.databinding.tool.reflection.annotation;

import android.databinding.Bindable;
import android.databinding.tool.reflection.ModelClass;
import android.databinding.tool.reflection.ModelMethod;
import android.databinding.tool.reflection.SdkUtil;
import android.databinding.tool.reflection.TypeUtil;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

class AnnotationMethod extends ModelMethod {
    final ExecutableType mMethod;
    final DeclaredType mDeclaringType;
    final ExecutableElement mExecutableElement;
    int mApiLevel = -1; // calculated on demand

    public AnnotationMethod(DeclaredType declaringType, ExecutableElement executableElement) {
        mDeclaringType = declaringType;
        mExecutableElement = executableElement;
        Types typeUtils = AnnotationAnalyzer.get().getTypeUtils();
        mMethod = (ExecutableType) typeUtils.asMemberOf(declaringType, executableElement);
    }

    @Override
    public ModelClass getDeclaringClass() {
        return new AnnotationClass(mDeclaringType);
    }

    @Override
    public ModelClass[] getParameterTypes() {
        List<? extends TypeMirror> parameters = mMethod.getParameterTypes();
        ModelClass[] parameterTypes = new ModelClass[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            parameterTypes[i] = new AnnotationClass(parameters.get(i));
        }
        return parameterTypes;
    }

    @Override
    public String getName() {
        return mExecutableElement.getSimpleName().toString();
    }

    @Override
    public ModelClass getReturnType(List<ModelClass> args) {
        TypeMirror returnType = mMethod.getReturnType();
        // TODO: support argument-supplied types
        // for example: public T[] toArray(T[] arr)
        return new AnnotationClass(returnType);
    }

    @Override
    public boolean isVoid() {
        return mMethod.getReturnType().getKind() == TypeKind.VOID;
    }

    @Override
    public boolean isPublic() {
        return mExecutableElement.getModifiers().contains(Modifier.PUBLIC);
    }

    @Override
    public boolean isStatic() {
        return mExecutableElement.getModifiers().contains(Modifier.STATIC);
    }

    @Override
    public boolean isBindable() {
        return mExecutableElement.getAnnotation(Bindable.class) != null;
    }

    @Override
    public int getMinApi() {
        if (mApiLevel == -1) {
            mApiLevel = SdkUtil.getMinApi(this);
        }
        return mApiLevel;
    }

    @Override
    public String getJniDescription() {
        return TypeUtil.getInstance().getDescription(this);
    }

    @Override
    public boolean isVarArgs() {
        return mExecutableElement.isVarArgs();
    }

    @Override
    public String toString() {
        return "AnnotationMethod{" +
                "mMethod=" + mMethod +
                ", mDeclaringType=" + mDeclaringType +
                ", mExecutableElement=" + mExecutableElement +
                ", mApiLevel=" + mApiLevel +
                '}';
    }
}
