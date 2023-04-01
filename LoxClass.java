import java.util.List;
import java.util.Map;


public class LoxClass implements LoxCallable {
		public String name;
		public Map<String, LoxFunction> methods;
		public LoxClass superclass;

		LoxClass(String name, Map<String, LoxFunction> methods, LoxClass superclass) {
				this.name = name;
				this.methods = methods;
				this.superclass = superclass;
		}

		public LoxFunction getMethod(String name) {
				if(methods.containsKey(name)) return methods.get(name);
				if(superclass != null) return superclass.getMethod(name);
				return null;
		}

		@Override
		public Object call(Interpreter interpreter, List<Object> arguments) {
				LoxInstance instance = new LoxInstance(this);
				LoxFunction initializer = methods.containsKey("init") ? methods.get("init") : null;
				if(initializer != null) initializer.bind(instance).call(interpreter, arguments);
				return instance;
		}

		@Override
		public int arity() {
				LoxFunction initializer = methods.containsKey("init") ? methods.get("init") : null;
				return initializer == null ? 0 : initializer.arity();
		}

		public String toString() {
				return name;
		}
}
