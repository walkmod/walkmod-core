import org.walkmod.javalang.ast.body.ModifierSet;
import java.lang.reflect.Modifier;
import org.walkmod.javalang.ast.body.FieldDeclaration;

for(type in node.types){  
  def fields = type.members.findAll({it instanceof FieldDeclaration});
  for (field in fields){
    int modifiers = ModifierSet.addModifier(field.modifiers, Modifier.PRIVATE);
    modifiers = ModifierSet.removeModifier(modifiers, Modifier.PROTECTED);
    modifiers = ModifierSet.removeModifier(modifiers, Modifier.PUBLIC);
    field.setModifiers(modifiers);
  }
}