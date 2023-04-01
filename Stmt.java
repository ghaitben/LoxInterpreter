import java.util.List;

abstract class Stmt {
		abstract <T> T accept(Visitor<T> visitor);

		interface Visitor<T> {
				T visitExpressionStmt(Expression stmt);
				T visitIfStmt(If stmt);
				T visitWhileStmt(While stmt);
				T visitPrintStmt(Print stmt);
				T visitVarStmt(Var stmt);
				T visitBlockStmt(Block stmt);
				T visitBreakStmt(Break stmt);
				T visitFunctionStmt(Function stmt);
				T visitReturnStmt(Return stmt);
				T visitClassStmt(Class stmt);
		}

		public static class Expression extends Stmt{
				Expr expression;

				Expression(Expr expression) {
						this.expression = expression;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitExpressionStmt(this);
				}
		}

		public static class If extends Stmt{
				Expr condition;
				Stmt thenBranch;
				Stmt elseBranch;

				If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
						this.condition = condition;
						this.thenBranch = thenBranch;
						this.elseBranch = elseBranch;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitIfStmt(this);
				}
		}

		public static class While extends Stmt{
				Expr condition;
				Stmt body;

				While(Expr condition, Stmt body) {
						this.condition = condition;
						this.body = body;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitWhileStmt(this);
				}
		}

		public static class Print extends Stmt{
				Expr expression;

				Print(Expr expression) {
						this.expression = expression;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitPrintStmt(this);
				}
		}

		public static class Var extends Stmt{
				Token name;
				Expr initializer;

				Var(Token name, Expr initializer) {
						this.name = name;
						this.initializer = initializer;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitVarStmt(this);
				}
		}

		public static class Block extends Stmt{
				List<Stmt> statements;

				Block(List<Stmt> statements) {
						this.statements = statements;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitBlockStmt(this);
				}
		}

		public static class Break extends Stmt{
				Expr condition;

				Break(Expr condition) {
						this.condition = condition;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitBreakStmt(this);
				}
		}

		public static class Function extends Stmt{
				Token name;
				List<Token> arguments;
				List<Stmt> body;

				Function(Token name, List<Token> arguments, List<Stmt> body) {
						this.name = name;
						this.arguments = arguments;
						this.body = body;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitFunctionStmt(this);
				}
		}

		public static class Return extends Stmt{
				Token keyword;
				Expr value;

				Return(Token keyword, Expr value) {
						this.keyword = keyword;
						this.value = value;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitReturnStmt(this);
				}
		}

		public static class Class extends Stmt{
				Token name;
				List<Stmt.Function> methods;
				Expr.Variable superclass;

				Class(Token name, List<Stmt.Function> methods, Expr.Variable superclass) {
						this.name = name;
						this.methods = methods;
						this.superclass = superclass;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitClassStmt(this);
				}
		}

}
