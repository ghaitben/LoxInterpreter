import java.util.List;

abstract class Expr {
		abstract <T> T accept(Visitor<T> visitor);

		interface Visitor<T> {
				T visitBinaryExpr(Binary expr);
				T visitGroupingExpr(Grouping expr);
				T visitLiteralExpr(Literal expr);
				T visitUnaryExpr(Unary expr);
				T visitVariableExpr(Variable expr);
				T visitAssignExpr(Assign expr);
				T visitLogicalExpr(Logical expr);
				T visitCallExpr(Call expr);
				T visitGetExpr(Get expr);
				T visitSetExpr(Set expr);
				T visitThisExpr(This expr);
				T visitSuperExpr(Super expr);
		}

		public static class Binary extends Expr{
				Expr left;
				Token operator;
				Expr right;

				Binary(Expr left, Token operator, Expr right) {
						this.left = left;
						this.operator = operator;
						this.right = right;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitBinaryExpr(this);
				}
		}

		public static class Grouping extends Expr{
				Expr expression;

				Grouping(Expr expression) {
						this.expression = expression;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitGroupingExpr(this);
				}
		}

		public static class Literal extends Expr{
				Object value;

				Literal(Object value) {
						this.value = value;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitLiteralExpr(this);
				}
		}

		public static class Unary extends Expr{
				Token operator;
				Expr right;

				Unary(Token operator, Expr right) {
						this.operator = operator;
						this.right = right;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitUnaryExpr(this);
				}
		}

		public static class Variable extends Expr{
				Token name;

				Variable(Token name) {
						this.name = name;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitVariableExpr(this);
				}
		}

		public static class Assign extends Expr{
				Token name;
				Expr value;

				Assign(Token name, Expr value) {
						this.name = name;
						this.value = value;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitAssignExpr(this);
				}
		}

		public static class Logical extends Expr{
				Expr left;
				Token operator;
				Expr right;

				Logical(Expr left, Token operator, Expr right) {
						this.left = left;
						this.operator = operator;
						this.right = right;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitLogicalExpr(this);
				}
		}

		public static class Call extends Expr{
				Expr callee;
				Token paren;
				List<Expr> arguments;

				Call(Expr callee, Token paren, List<Expr> arguments) {
						this.callee = callee;
						this.paren = paren;
						this.arguments = arguments;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitCallExpr(this);
				}
		}

		public static class Get extends Expr{
				Expr object;
				Token name;

				Get(Expr object, Token name) {
						this.object = object;
						this.name = name;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitGetExpr(this);
				}
		}

		public static class Set extends Expr{
				Expr object;
				Token name;
				Expr value;

				Set(Expr object, Token name, Expr value) {
						this.object = object;
						this.name = name;
						this.value = value;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitSetExpr(this);
				}
		}

		public static class This extends Expr{
				Token keyword;

				This(Token keyword) {
						this.keyword = keyword;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitThisExpr(this);
				}
		}

		public static class Super extends Expr{
				Token keyword;
				Token method;

				Super(Token keyword, Token method) {
						this.keyword = keyword;
						this.method = method;
				}

				@Override
				<T> T accept(Visitor<T> visitor) {
						return visitor.visitSuperExpr(this);
				}
		}

}
