import java.util.Map;
import java.util.HashMap;

public class LoxInstance {
		private LoxClass klass;
		private Map<String, Object> fields = new HashMap<>();

		LoxInstance(LoxClass klass) {
				this.klass = klass;
		}

		public Object get(Token name) {
				if(fields.containsKey(name.lexeme)) return fields.get(name.lexeme);

				LoxFunction method = klass.getMethod(name.lexeme);
				if(method != null) return method.bind(this);

				throw new RuntimeException("Undefined property");
		}

		public void set(Token name, Object value) {
				fields.put(name.lexeme, value);
		}

		@Override
		public String toString() {
				return klass.name + " instance";
		}
}
