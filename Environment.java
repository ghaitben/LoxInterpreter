import java.util.Map;
import java.util.HashMap;

public class Environment {
		public Map<String, Object> values = new HashMap<String, Object>();
		public Environment enclosing;

		Environment() {
				enclosing = null;
		}

		Environment(Environment enclosing) {
				this.enclosing = enclosing;
		}

		public void define(String name, Object value) {
				values.put(name, value);
		}

		public Object get(Token name) {
				if(values.containsKey(name.lexeme)) return values.get(name.lexeme);
				if(enclosing != null) return enclosing.get(name);
				throw new RuntimeException("Undefined variable " + name.lexeme + " at line " + name.line);
		}

		public Object assign(Token name, Object value) {
				if(values.containsKey(name.lexeme)) {
						values.put(name.lexeme, value);
						return value;
				}
				if(enclosing != null) return enclosing.assign(name, value);
				throw new RuntimeException("Undefined variable " + name.lexeme + " at line " + name.line);
		}

		public Object getAt(Integer distance, Token name) {
				return ancestor(distance).values.get(name.lexeme);
		}

		public void assignAt(Integer distance, Token name, Object value) {
				ancestor(distance).values.put(name.lexeme, value);
		}

		public Environment ancestor(int distance) {
				Environment ret = this;
				while(distance-- > 0) ret = ret.enclosing;
				return ret;
		}

		public void display() {
				values.forEach((key, value) -> System.out.println(key + ":" + value));
		}
}
