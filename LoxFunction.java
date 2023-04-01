import java.util.List;

public class LoxFunction implements LoxCallable {
		private Stmt.Function declaration;
		private Environment closure;
		private boolean isInitializer;

		LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
				this.declaration = declaration;
				this.closure = closure;
				this.isInitializer = isInitializer;
		}

		public LoxFunction bind(LoxInstance instance) {
				Environment implicit_environment = new Environment(closure);
				implicit_environment.define("this", instance);
				return new LoxFunction(declaration, implicit_environment, isInitializer);
		}

		@Override
		public int arity() {
				return declaration.arguments.size();
		}

		@Override
		public Object call(Interpreter interpreter, List<Object> arguments) {
				Environment env = new Environment(closure);

				for(int i = 0; i < declaration.arguments.size(); ++i) {
						env.define(declaration.arguments.get(i).lexeme, arguments.get(i));
				}

				try {
						interpreter.executeBlock(declaration.body, env);
				}
				catch(Interpreter.Return returnValue) {
						if(isInitializer) return closure.getAt(0, new Token(TokenType.THIS, "this", "this", -1));
						return returnValue.value;
				}
				return null;
		}
}
