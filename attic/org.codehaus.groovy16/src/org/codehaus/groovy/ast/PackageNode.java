/*
 * Copyright 2003-2009 the original author or authors.
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

// FIXASC (groovychange)
// old code

/**
 * Represents a package in the AST.
 *
 * @author Paul King
 */
 /*
public class PackageNode extends AnnotatedNode {
    private String name;

    public PackageNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the text display of this package definition
     *
    public String getText() {
        return "package " + name;
    }

    public void visit(GroovyCodeVisitor visitor) {
    }
}
*/
// newcode
/**
 * Represents a package declaration
 * 
 * @author Andy Clement
 */
public class PackageNode extends AnnotatedNode {

    private final String packageName;
    
    public PackageNode(String packageName) {
        this.packageName = packageName;
    }
    
    /**
     * @return the text display of this import
     */
    public String getText() {
    	return "package "+packageName;
    }
   
    public String getPackageName() {
    	return packageName;
    }
    
    public void visit(GroovyCodeVisitor visitor) {
    }

}
// end