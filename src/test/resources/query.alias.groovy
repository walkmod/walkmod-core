import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;

type =  root.types[0];

org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration.metaClass.getFields = { ->delegate.members.findAll({it instanceof FieldDeclaration}); }
org.walkmod.javalang.ast.body.TypeDeclaration.metaClass.getMethods = { ->delegate.members.findAll({it instanceof MethodDeclaration}); }

