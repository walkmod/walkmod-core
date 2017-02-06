package ${query.resolve("root.package.name")};

import javax.persistence.Id;import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

public class ${query.resolve("type.name")}{

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE)
   Integer id;

   @Override
        public boolean equals(Object o) {
                if (o instanceof ${query.resolve("type.name")}) {
                        return id.equals(((${query.resolve("type.name")}) o).id);
                }
                return false;
        }

   @Override
        public int hashCode() {
                return id.hashCode();
        }

}