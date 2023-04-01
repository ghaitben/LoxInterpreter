import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

		public static class BreakError extends RuntimeException {}

		public static class Return extends RuntimeException {
				public final Object value;

				Return(Object value) {
						super(null, null, false, false);
						this.value = value;
				}
		}

		public Environment globals = new Environment();
		public Environment environment = globals;
		public Map<Expr, Integer> locals = new HashMap<>();

		Interpreter() {
				globals.define("clock", new LoxCallable() {
						@Override
						public int arity() { return 0; }

						@Override
						public Object call(Interpreter interpreter, List<Object> arguments) {
								return (double) System.currentTimeMillis() / 1000.0;
						}
				});
		}

		public void resolve(Expr expr, int depth) {
				locals.put(expr, depth);
		}

		public void interpret(List<Stmt> statements) {
				try {
						for(Stmt s : statements) {
								s.accept(this);
						}
				}
				catch(BreakError e) {
						System.out.println("Can't use break outside of a loop");
						return;
				}
				catch(RuntimeException e) {
						Lox.runtimeError(e);
						return;
				}
		}

		@Override
		public Object visitBinaryExpr(Expr.Binary expr) {
				Object left = expr.left.accept(this);
				Object right = expr.right.accept(this);
				checkOperatorTypesOrReturnError(expr.operator, left, right);

				switch(expr.operator.type) {
						case PLUS:
								if(left instanceof String) return (String) left + (String) right;
								if(left instanceof Double) return (double) left + (double) right;
						case MINUS:
								return (double) left - (double) right;
						case STAR:
								return (double) left * (double) right;
						case SLASH:
								return (double) left / (double) right;
						case GREATER:
								return (double) left > (double) right;
						case GREATER_EQUAL:
								return (double) left >= (double) right;
						case LESS:
						case LESS_EQUAL:
								return (double) left <= (double) right;
						case EQUAL_EQUAL:
								return isEqual(left, right);
						case BANG_EQUAL:
								return !isEqual(left, right);
				}
				return null;
		}

		@Override
		public Object visitUnaryExpr(Expr.Unary expr) {
				Object right = expr.right.accept(this);
				checkOperatorTypesOrReturnError(expr.operator, right);

				switch(expr.operator.type) {
						case MINUS:
								return -(double) right;
						case BANG:
								return !isTruthy(right);
				}
				return null;
		}

		@Override
		public Object visitGroupingExpr(Expr.Grouping expr) {
				return expr.expression.accept(this);
		}

		@Override
		public Object visitLiteralExpr(Expr.Literal expr) {
				return expr.value;	
		}

		@Override
		public Object visitVariableExpr(Expr.Variable expr) {
				return lookUpVariable(expr.name, expr);
		}

		@Override
		public Object visitAssignExpr(Expr.Assign expr) {
				Object value = expr.value.accept(this);

				Integer distance = locals.get(expr);
				if(distance != null) { environment.assignAt(distance, expr.name, value); }
				else { globals.assign(expr.name, value); }
				return value;
		}

		@Override
		public Object visitLogicalExpr(Expr.Logical expr) {
				Object left = expr.left.accept(this);
				if(expr.operator.type == TokenType.OR) {
						if(isTruthy(left)) return true;
				}
				else {
						if(!isTruthy(left)) return false;
				}

				return expr.right.accept(this);
		}

		@Override
		public Object visitGetExpr(Expr.Get expr) {
				Object object = expr.object.accept(this);

				if(object instanceof LoxInstance) {
						return ((LoxInstance) object).get(expr.name);
				}
				throw new RuntimeException("Only instances have properties");
		}

		@Override
		public Object visitSetExpr(Expr.Set expr) {
				Object object = expr.object.accept(this);

				if(!(object instanceof LoxInstance)) throw new RuntimeException("only instances have fields");

				Object value = expr.value.accept(this);

				((LoxInstance) object).set(expr.name, value);
				return value;
		}

		@Override
		public Object visitCallExpr(Expr.Call expr) {
				Object callee = expr.callee.accept(this);
				List<Object> arguments = new ArrayList<Object>();

				for(Expr e : expr.arguments) {
						arguments.add(e.accept(this));
				}
				
				if(!(callee instanceof LoxCallable)) throw new RuntimeException("Can only call functions and classes");

				LoxCallable function = (LoxCallable) callee;

				if (arguments.size() != function.arity()) {
					throw new RuntimeException("Expected " +
							function.arity() + " arguments but got " +
							arguments.size() + "." + " At line " + expr.paren.line);
				}

				return function.call(this, arguments);
		}

		@Override
		public Object visitThisExpr(Expr.This expr) {
				return lookUpVariable(expr.keyword, expr);
		}

		@Override
		public Object visitSuperExpr(Expr.Super expr) {
				int distance = locals.get(expr);
				LoxClass superclass = (LoxClass) environment.getAt(distance, expr.keyword);
				LoxInstance object = (LoxInstance) environment.getAt(distance - 1, new Token(null, "this", null, -1));

				LoxFunction method = superclass.getMethod(expr.method.lexeme);

				if(method == null) 
						throw new RuntimeException("Unable to find method name in superclass");
				
				return method.bind(object);
		}

		@Override
		public Void visitExpressionStmt(Stmt.Expression statement) {
				statement.expression.accept(this);
				return null;
		}

		@Override
		public Void visitReturnStmt(Stmt.Return statement) {
				Object value = statement.value == null ? null : statement.value.accept(this);
				throw new Return(value);
		}

		@Override
		public Void visitPrintStmt(Stmt.Print statement) {
				Object expression_to_print = statement.expression.accept(this);
				System.out.println(expression_to_print);
				return null;
		}

		@Override
		public Void visitVarStmt(Stmt.Var statement) {
				Object value = statement.initializer == null ? null : statement.initializer.accept(this);
				environment.define(statement.name.lexeme, value);
				return null;
		} 

		@Override
		public Void visitBlockStmt(Stmt.Block statement) {
				executeBlock(statement.statements, new Environment(environment));
				return null;
		}

		public void executeBlock(List<Stmt> statements, Environment env) {
				Environment previous = this.environment;

				try {
						this.environment = env;
						for(Stmt s : statements) s.accept(this);
				}
				finally {
						this.environment = previous;
				}
		}

		@Override
		public Void visitIfStmt(Stmt.If statement) {
				boolean condition = isTruthy(statement.condition.accept(this));
				if(condition) {
						statement.thenBranch.accept(this);
				}
				else if(statement.elseBranch != null) {
						statement.elseBranch.accept(this);
				}
				return null;
		}

		@Override
		public Void visitWhileStmt(Stmt.While statement) {
				try {
						while(isTruthy(statement.condition.accept(this))) {
								statement.body.accept(this);
						}
				}
				catch(BreakError e) {
						environment = environment.enclosing;
				}
				return null;
		}

		@Override
		public Void visitFunctionStmt(Stmt.Function statement) {
				LoxFunction function = new LoxFunction(statement, environment, false);
				environment.define(statement.name.lexeme, function);
				return null;
		}

		@Override
		public Void visitBreakStmt(Stmt.Break statement) {
				throw new BreakError();
		}

		@Override
		public Void visitClassStmt(Stmt.Class statement) {
				environment.define(statement.name.lexeme, null);

				Object superclass = statement.superclass != null ? statement.superclass.accept(this) : null;
				if(superclass != null && !(superclass instanceof LoxClass))
						throw new RuntimeException("superclass must be a class");
				
				if(superclass != null) {
						environment = new Environment(environment);
						environment.define("super", superclass);
				}

				Map<String, LoxFunction> methods = new HashMap<>();
				for(Stmt.Function method : statement.methods) {
						boolean isInitializer = method.name.lexeme.equals("init");
						methods.put(method.name.lexeme, new LoxFunction(method, environment, isInitializer));
				}

				LoxClass klass = new LoxClass(statement.name.lexeme, methods, (LoxClass) superclass);

				if(superclass != null) {
						environment = environment.enclosing;
				}

				environment.assign(statement.name, klass);
				return null;
		}

		private Object lookUpVariable(Token name, Expr expr) {
				Integer distance = locals.get(expr);
				if(distance != null) return environment.getAt(distance, name);
				return globals.get(name);
		}

		private boolean isTruthy(Object a) {
				if(a == null) return false;
				if(a instanceof Boolean) return (boolean) a;
				return true;
		}

		private boolean isEqual(Object a, Object b) {
				if(a == null || b == null) return a == b;
				return a.equals(b);
		}

		private void checkOperatorTypesOrReturnError(Token token, Object left, Object right) {
				if(token.type == TokenType.EQUAL_EQUAL || token.type == TokenType.BANG_EQUAL) return;
				if(left instanceof Double && right instanceof Double) return;
				if(left instanceof String && right instanceof String && token.type == TokenType.PLUS) return;
				throw new RuntimeException("type mismatch for binary operator " + token.lexeme + " on line " + token.line);
		}

		private void checkOperatorTypesOrReturnError(Token token, Object right) {
				if(right instanceof Double) return;
				throw new RuntimeException("type mismatch for unary operator " + token.lexeme + " on line " + token.line);
		}
}
