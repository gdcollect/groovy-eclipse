/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast;

import groovyjarjarasm.asm.Opcodes;

import java.util.List;

import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Represents a method declaration
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision: 20220 $
 */
public class MethodNode extends AnnotatedNode implements Opcodes {

    private final String name;
    private int modifiers;
    private ClassNode returnType;
    private Parameter[] parameters;
    private boolean hasDefaultValue = false;
    private Statement code;
    private boolean dynamicReturnType;
    private VariableScope variableScope;
    private final ClassNode[] exceptions;
    private final boolean staticConstructor;
    
    // type spec for generics
    private GenericsType[] genericsTypes=null;
    private boolean hasDefault;
    
    // cached data
    String typeDescriptor;

    public MethodNode(String name, int modifiers, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        this.name = name;
        this.modifiers = modifiers;
        this.code = code;
        setReturnType(returnType); 
        VariableScope scope = new VariableScope();
        setVariableScope(scope);
        setParameters(parameters);
        this.hasDefault = false;       
        this.exceptions = exceptions;
        this.staticConstructor = (name != null && name.equals("<clinit>"));
    }

    /**
     * The type descriptor for a method node is a string containing the name of the method, its return type,
     * and its parameter types in a canonical form. For simplicity, I'm using the format of a Java declaration
     * without parameter names.
     *
     * @return the type descriptor
     */
    public String getTypeDescriptor() {
        if (typeDescriptor==null) { 
            StringBuffer buf = new StringBuffer(name.length()+parameters.length*10);
            buf.append(returnType.getName());
            buf.append(' ');
            buf.append(name);
            buf.append('(');
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Parameter param = parameters[i];
                buf.append(param.getType().getName());
            }
            buf.append(')');
            typeDescriptor = buf.toString();
        }
        return typeDescriptor;
    }
    
    private void invalidateCachedData() {
        typeDescriptor = null;
    }
 
    public boolean isVoidMethod() {
        return returnType==ClassHelper.VOID_TYPE;
    }

    public Statement getCode() {
        return code;
    }

    public void setCode(Statement code) {
        this.code = code;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        invalidateCachedData();
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        invalidateCachedData();
        VariableScope scope = new VariableScope();
        this.parameters = parameters;
        if (parameters != null && parameters.length > 0) {
            for (Parameter para : parameters) {
                if (para.hasInitialExpression()) {
                    this.hasDefaultValue = true;
                }
                para.setInStaticContext(isStatic());
                scope.putDeclaredVariable(para);
            }
        }
        setVariableScope(scope);
    }
    
    public ClassNode getReturnType() {
        return returnType;
    }

    public VariableScope getVariableScope() {
        return variableScope;
    }

    public void setVariableScope(VariableScope variableScope) {
        this.variableScope = variableScope;
        variableScope.setInStaticContext(isStatic());
    }

    public boolean isDynamicReturnType() {
        return dynamicReturnType;
    }

    public boolean isAbstract() {
        return (modifiers & ACC_ABSTRACT) != 0;
    }

    public boolean isStatic() {
        return (modifiers & ACC_STATIC) != 0;
    }

    public boolean isPublic() {
        return (modifiers & ACC_PUBLIC) != 0;
    }

    public boolean isPrivate() {
        return (modifiers & ACC_PRIVATE) != 0;
    }

    public boolean isProtected() {
        return (modifiers & ACC_PROTECTED) != 0;
    }

    public boolean hasDefaultValue() {
        return this.hasDefaultValue;
    }

    public String toString() {
        return "MethodNode@"+hashCode()+"[" + getTypeDescriptor() + "]";
    }

    public void setReturnType(ClassNode returnType) {
        invalidateCachedData();
    	dynamicReturnType |= ClassHelper.DYNAMIC_TYPE==returnType;
        this.returnType = returnType;
        if (returnType==null) this.returnType = ClassHelper.OBJECT_TYPE;
    }

    public ClassNode[] getExceptions() {
        return exceptions;
    }
    
    public Statement getFirstStatement(){
        if (code == null) return null;
        Statement first = code;
        while (first instanceof BlockStatement) {
            List<Statement> list = ((BlockStatement) first).getStatements();
            if (list.isEmpty()) {
                first=null;
            } else {
                first = list.get(0);
            }
        }
        return first;
    }
    
    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    public void setGenericsTypes(GenericsType[] genericsTypes) {
        invalidateCachedData();
        this.genericsTypes = genericsTypes;
    }

    public void setAnnotationDefault(boolean b) {
        this.hasDefault = b;
    }

    public boolean hasAnnotationDefault() {
        return hasDefault;
    }

    public boolean isStaticConstructor() {
        return staticConstructor;
    }
    
    // GRECLIPSE: start
    /**
     * When default parameters are involved, this field will be
     * the original method without any default parameters applied
     */
    private MethodNode original = this;
    
    /**
     * @return When default parameters are involved, this method returns the {@link MethodNode} 
     * where no default parameters have been applied.  Otherwise returns <code>this</code>.  Never
     * returns null.
     */
    public MethodNode getOriginal() {
        return original;
    }
    
    public void setOriginal(MethodNode original) {
        this.original = original;
    }
    // GRECLIPSE: end
    
    // GRECLIPSE: start
    // backported from 1.8 branch
    /**
     * @return true if this method is the run method from a script
     */
    public boolean isScriptBody() {
        return getDeclaringClass() != null &&
                getDeclaringClass().isScript() &&
                getName().equals("run") &&
                // GRECLIPSE: start
                /*was{
                getColumnNumber() == -1;
                }*/
                // now:
                (parameters==null || parameters.length==0) &&
                (returnType!=null && returnType.getName().equals("java.lang.Object"));
                // GRECLIPSE: end
    }
    // GRECLIPSE: end
}