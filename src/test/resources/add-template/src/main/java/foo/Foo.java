package foo;

import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

public class Bar {
   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE)
   Integer id;

   @Override
   public boolean equals(Object o) {
      if (o instanceof Bar) {
         return id.equals(((Bar) o).id);
      }
      return false;
   }

   @Override
   public int hashCode() {
      return id.hashCode();
   }
}

public class Bar {
   public String bar;
}