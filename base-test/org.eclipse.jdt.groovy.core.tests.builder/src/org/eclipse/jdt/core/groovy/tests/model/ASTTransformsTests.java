/*
 * Copyright 2009-2016 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.model;

import java.util.List;

import junit.framework.Test;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;

/**
 *
 * @author Andrew Eisenberg
 * @created May 3, 2010
 */
public class ASTTransformsTests extends BuilderTests {

    public static Test suite() {
        return buildTestSuite(ASTTransformsTests.class);
    }

    public ASTTransformsTests(String name) {
        super(name);
    }

    private IProject project;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IPath projectPath = env.addProject("Project");
        project = ResourcesPlugin.getWorkspace().getRoot().getProject("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");
        env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");
    }

    public void testDelegateAnnotationFromOtherField() throws Exception {
        createUnit("Other",
                "class Other {\n" +
                "  @Delegate Date me\n" +
                "  int compareTo(arg0) { }\n" +
                "}");

        GroovyCompilationUnit unit = createUnit("ThisUnit", "Other");
        env.fullBuild();
        expectingNoProblems();
        FieldNode field = getMeField(unit);
        assertAnnotation("groovy.lang.Delegate", field);
    }

    public void testDelegateAnnotationFromOtherMethod() throws Exception {
        createUnit("Other",
                "class Other {\n" +
                "  @Delegate Date me\n" +
                "  @Newify int compareTo(arg0) { }\n" +
                "}");

        GroovyCompilationUnit unit = createUnit("ThisUnit", "Other");
        env.fullBuild();
        expectingNoProblems();
        MethodNode method = getMethod(unit,"compareTo");
        assertAnnotation("groovy.lang.Newify", method);
    }

    public void testSingletonAnnotationFromOtherClass() throws Exception {
        createUnit("Other",
                "@Singleton class Other { }");

        GroovyCompilationUnit unit = createUnit("ThisUnit", "Other");
        env.fullBuild();
        expectingNoProblems();
        ClassNode clazz = getClassFromScript(unit);
        assertAnnotation("groovy.lang.Singleton", clazz);
    }

    private void assertAnnotation(String aName, AnnotatedNode node) {
        assertEquals("Expecting @" + aName + " but no annotations found.", 1, node.getAnnotations().size());
        assertEquals(aName, node.getAnnotations().get(0).getClassNode().getName());
    }

    private FieldNode getMeField(GroovyCompilationUnit unit) {
        ClassNode clazz = getClassFromScript(unit);
        clazz.getFields();  // force lazy initialization
        return clazz.getField("me");
    }

    private MethodNode getMethod(GroovyCompilationUnit unit,String name) {
        ClassNode clazz = getClassFromScript(unit);
        clazz.getFields();  // force lazy initialization
        List<MethodNode> ms = clazz.getMethods();
        for (MethodNode m: ms) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    public void testImmutableAnnotation1() throws Exception {
        GroovyCompilationUnit unit = createUnit("Thiz", "import groovy.transform.Immutable\n @Immutable class Thiz { String foo }");
        env.fullBuild();
        expectingNoProblems();
        IType type = unit.getType("Thiz");
        boolean foundConstructor = false;
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
            if (method.isConstructor()) {
                if (foundConstructor) {
                    fail("Should have found exactly one constructor");
                }
                foundConstructor = true;
                ILocalVariable[] parameters = method.getParameters();
                assertEquals("Should have exactly one argument to constructor.", 1, parameters.length);
                assertEquals("Should be type string", "QString;", parameters[0].getTypeSignature());
            }
        }

        if (!foundConstructor) {
            fail("Should have found exactly one constructor");
        }
    }

    public void testImmutableAnnotation1a() throws Exception {
        GroovyCompilationUnit unit = createUnit("Thiz", "@groovy.transform.Immutable class Thiz { String foo }");
        env.fullBuild();
        expectingNoProblems();
        IType type = unit.getType("Thiz");
        boolean foundConstructor = false;
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
            if (method.isConstructor()) {
                if (foundConstructor) {
                    fail("Should have found exactly one constructor");
                }
                foundConstructor = true;
                ILocalVariable[] parameters = method.getParameters();
                assertEquals("Should have exactly one argument to constructor.", 1, parameters.length);
                assertEquals("Should be type string", "QString;", parameters[0].getTypeSignature());
            }
        }

        if (!foundConstructor) {
            fail("Should have found exactly one constructor");
        }
    }

    public void testImmutableAnnotation2() throws Exception {
        GroovyCompilationUnit unit = createUnit("Thiz", "import groovy.transform.Immutable\n @Immutable class Thiz { }");
        env.fullBuild();
        expectingNoProblems();
        IType type = unit.getType("Thiz");
        int constructorCount = 0;
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
            if (method.isConstructor()) {
                constructorCount++;
            }
        }
        assertEquals("Should have found no constructors", 0, constructorCount);
    }

    public void testImmutableAnnotation3() throws Exception {
        createUnit("p", "Immutable", "package p\n@interface Immutable { }");
        GroovyCompilationUnit unit = createUnit("Thiz", "import p.Immutable\n@Immutable class Thiz { String foo }");
        env.fullBuild();
        expectingNoProblems();
        IType type = unit.getType("Thiz");
        IMethod[] methods = type.getMethods();
        int constructorCount = 0;
        for (IMethod method : methods) {
            if (method.isConstructor()) {
                constructorCount++;
            }
        }
        assertEquals("Should have found no constructors", 0, constructorCount);
    }

    private ClassNode getClassFromScript(GroovyCompilationUnit unit) {
        return ((ClassExpression) ((ReturnStatement) unit.getModuleNode().getStatementBlock().getStatements().get(0)).getExpression()).getType();
    }

    protected GroovyCompilationUnit createUnit(String name, String contents) {
        IPath path = env.addGroovyClass(project.getFolder("src").getFullPath(), name, contents);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }

    protected GroovyCompilationUnit createUnit(String pkg, String name, String contents) {
        IPath pkgPath = env.addPackage(project.getFolder("src").getFullPath(), pkg);
        IPath path = env.addGroovyClass(pkgPath, name, contents);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }
}
