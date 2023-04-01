import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
		private enum FunctionType {
				NONE, FUNCTION, METHOD, INITIALIZER
		}

		private enum ClassType {
				NONE, CLASS, SUBCLASS
		}

		private Interpreter interpreter;
		private Stack<Map<String, Boolean>> scopes = new Stack<>();
		private FunctionType currentFunctionType = FunctionType.NONE;
		private ClassType currentClassType = ClassType.NONE;

		public Resolver(Interpreter interpreter) {
				this.interpreter = interpreter;
		}
		
		@Override
		public Void visitBlockStmt(Stmt.Block stmt) {
				scopes.push(new HashMap<String, Boolean>());
				for(Stmt s : stmt.statements) s.accept(this);
				scopes.pop();
				return null;
		}

		@Override
		public Void visitVarStmt(Stmt.Var stmt) {
				declare(stmt.name);
				if(stmt.initializer != null) stmt.initializer.accept(this);
				define(stmt.name);
				return null;
		}

		@Override
		public Void visitVariableExpr(Expr.Variable expr) {
				if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
						System.out.println("Problem initializing variable");
				}
				resolveLocal(expr, expr.name);
				return null;
		}

		@Override
		public Void visitAssignExpr(Expr.Assign expr) {
				expr.value.accept(this);
				resolveLocal(expr, expr.name);
				return null;
		}

		@Override
		public Void visitFunctionStmt(Stmt.Function stmt) {
				declare(stmt.name);
				define(stmt.name);
				resolveFunction(stmt, FunctionType.FUNCTION);
				return null;
		}

		@Override
		public Void visitExpressionStmt(Stmt.Expression stmt) {
				stmt.expression.accept(this);
				return null;
		}

		@Override
		public Void visitClassStmt(Stmt.Class stmt) {
				ClassType previous = currentClassType;
				currentClassType = ClassType.CLASS;

				declare(stmt.name);
				define(stmt.name);

				if(stmt.superclass != null && stmt.superclass.name.lexeme.equals(stmt.name.lexeme))
						throw new RuntimeException("a class can't inherit from itself");

				if(stmt.superclass != null) {
						currentClassType = ClassType.SUBCLASS;
						stmt.superclass.accept(this);

						scopes.push(new HashMap<String, Boolean>());
						scopes.peek().put("super", true);
				}




				scopes.push(new HashMap<String, Boolean>());
				scopes.peek().put("this", true);

				for(Stmt.Function method : stmt.methods) {
						FunctionType type = method.name.lexeme.equals("init") ?
								                FunctionType.INITIALIZER : FunctionType.METHOD;
						resolveFunction(method, type);
				}

				scopes.pop();

				if(stmt.superclass != null) {
						scopes.pop();
						currentClassType = ClassType.CLASS;
				}

				currentClassType = previous;
				return null;
		}

		@Override
		public Void visitSuperExpr(Expr.Super expr) {
				if (currentClassType == ClassType.NONE || currentClassType != ClassType.SUBCLASS)
						throw new RuntimeException("can't use the keyword super outside of a class");
				
				resolveLocal(expr, expr.keyword);
				return null;
		}

		@Override
		public Void visitSetExpr(Expr.Set expr) {
				expr.object.accept(this);
				expr.value.accept(this);
				return null;
		}

		@Override
		public Void visitGetExpr(Expr.Get expr) {
				expr.object.accept(this);
				return null;
		}

		@Override
		public Void visitThisExpr(Expr.This expr) {
				if (currentClassType != ClassType.CLASS) {
						throw new RuntimeException("Can't use 'this' outside of class definition");
				}

				resolveLocal(expr, expr.keyword);
				return null;
		}

		@Override
		public Void visitIfStmt(Stmt.If stmt) {
				stmt.condition.accept(this);
				stmt.thenBranch.accept(this);
				if(stmt.elseBranch != null) stmt.elseBranch.accept(this);
				return null;
		}

		@Override
		public Void visitPrintStmt(Stmt.Print stmt) {
				stmt.expression.accept(this);
				return null;
		}

		@Override
		public Void visitReturnStmt(Stmt.Return stmt) {
				if(currentFunctionType == FunctionType.NONE) 
						throw new RuntimeException("can't return from top-level code");

				if(currentFunctionType == FunctionType.INITIALIZER) 
						throw new RuntimeException("can't return a value from an initializer");

				if(stmt.value != null) stmt.value.accept(this);
				return null;
		}

		@Override
		public Void visitWhileStmt(Stmt.While stmt) {
				stmt.condition.accept(this);
				stmt.body.accept(this);
				return null;
		}

		@Override
		public Void visitBinaryExpr(Expr.Binary expr) {
				expr.left.accept(this);
				expr.right.accept(this);
				return null;
		}

		@Override
		public Void visitCallExpr(Expr.Call expr) {
				expr.callee.accept(this);
				for(Expr arg : expr.arguments) arg.accept(this);
				return null;
		}

		@Override
		public Void visitGroupingExpr(Expr.Grouping expr) {
				expr.expression.accept(this);
				return null;
		}

		@Override
		public Void visitLiteralExpr(Expr.Literal expr) {
				return null;
		}

		@Override
		public Void visitLogicalExpr(Expr.Logical expr) {
				expr.left.accept(this);
				expr.right.accept(this);
				return null;
		}

		@Override
		public Void visitUnaryExpr(Expr.Unary expr) {
				expr.right.accept(this);
				return null;
		}

		@Override
		public Void visitBreakStmt(Stmt.Break stmt) {
				return null;
		}

		private void resolveFunction(Stmt.Function function, FunctionType type) {
				FunctionType enclosingFunctionType = currentFunctionType;
				currentFunctionType = type;
				scopes.push(new HashMap<String, Boolean>());
				for(Token param : function.arguments) {
						declare(param);
						define(param);
		}
				for(Stmt s : function.body) s.accept(this);
				currentFunctionType = enclosingFunctionType;
				scopes.pop();
		}

		private void resolveLocal(Expr expr, Token name) {
				for(int i = scopes.size() - 1; i >= 0; --i) {
						if(!scopes.get(i).containsKey(name.lexeme)) continue;
						interpreter.resolve(expr, scopes.size() - i - 1);
						return;
				}	
		}

		public void declare(Token name) {
				if(scopes.isEmpty()) return;
				if(scopes.peek().containsKey(name.lexeme)) throw new RuntimeException("variable already declared");
				scopes.peek().put(name.lexeme, false);
		}

		public void define(Token name) {
				if(scopes.isEmpty()) return;
				scopes.peek().put(name.lexeme, true);
		}
}
