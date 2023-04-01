import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser {

		private static class ParseError extends RuntimeException {}

		private List<Token> tokens;
		private int current = 0;

		Parser(List<Token> tokens) {
				this.tokens = tokens;
		}

		public List<Stmt> parse() {
				try {
						List<Stmt> statements = new ArrayList<Stmt>();
						while(!reachedEOF()) {
								statements.add(declaration());
						}
						return statements;
				}
				catch(RuntimeException e) {
						System.out.println("Runtime exception for some reason");
				}
				return null;
		}

		private Stmt declaration() {
				if(matchAny(TokenType.VAR)) return varDeclaration();
				return statement();
		}

		private Stmt varDeclaration() {
				Token name = consumeOrError(TokenType.IDENTIFIER, "Expected Identifier name");
				Expr initializer = matchAny(TokenType.EQUAL) ? expression() : null;
				consumeOrError(TokenType.SEMICOLON, "Expected ';' after variable declaration");
				return new Stmt.Var(name, initializer);
		}

		private Stmt statement() {
				if(matchAny(TokenType.PRINT)) return printStatement();
				if(matchAny(TokenType.LEFT_BRACE)) return block();
				if(matchAny(TokenType.IF)) return ifStatement();
				if(matchAny(TokenType.WHILE)) return whileStatement();
				if(matchAny(TokenType.FOR)) return forStatement();
				if(matchAny(TokenType.BREAK)) return breakStatement();
				if(matchAny(TokenType.FUN)) return function("function");
				if(matchAny(TokenType.RETURN)) return returnStatement();
				if(matchAny(TokenType.CLASS)) return classStatement();
				return expressionStatement();
		}

		private Stmt classStatement() {
				Token name = consumeOrError(TokenType.IDENTIFIER, "Expected identifier at the beginning of class");

				Expr.Variable superclass = null;
				if(matchAny(TokenType.LESS)) {
						consumeOrError(TokenType.IDENTIFIER, "Expected the name of a super class");
						superclass = new Expr.Variable(previous());
				}
				consumeOrError(TokenType.LEFT_BRACE, "Expected left brace at the beginning of the class definition");

				List<Stmt.Function> methods = new ArrayList<>();
				while(!check(TokenType.RIGHT_BRACE) && !reachedEOF()) {
						methods.add(function("method"));
				}
				consumeOrError(TokenType.RIGHT_BRACE, "Expected right brace at the end of a class declaration");

				return new Stmt.Class(name, methods, superclass);
		}

		private Stmt returnStatement() {
				Token token = previous();
				Expr returnExpression = !check(TokenType.SEMICOLON) ? expression() : null;
				consumeOrError(TokenType.SEMICOLON, "Expected ';' at the end of the return statement");

				return new Stmt.Return(token, returnExpression);
		}

		private Stmt.Function function(String kind) {
				Token name = consumeOrError(TokenType.IDENTIFIER, "Expected an identifier after fun");
				consumeOrError(TokenType.LEFT_PAREN, "Expected '(' after fun declration");

				List<Token> parameters = new ArrayList<Token>();

				if(!check(TokenType.RIGHT_PAREN)) {
						do {
								if(parameters.size() > 255) throw new RuntimeException("too many arguments");
								parameters.add(consumeOrError(TokenType.IDENTIFIER, "expected argument"));
						} while(matchAny(TokenType.COMMA));
				}

				consumeOrError(TokenType.RIGHT_PAREN, "Expected ')' after function declaration");
				consumeOrError(TokenType.LEFT_BRACE, "Expected a '{' after function def");

				List<Stmt> body = getBlockStatements();
				return new Stmt.Function(name, parameters, body);
		}

		private Stmt breakStatement() {
				consumeOrError(TokenType.SEMICOLON, "Expected ';' after break");
				return new Stmt.Break(null);
		}

		private Stmt forStatement() {
				consumeOrError(TokenType.LEFT_PAREN, "Expected '(' at the start of a for loop");
				Stmt initializer = matchAny(TokenType.VAR) ? varDeclaration() : null;
				if(initializer  == null) {
						initializer = matchAny(TokenType.SEMICOLON) ? null : expressionStatement();
				}

				Expr condition = !check(TokenType.SEMICOLON) ? expression() : null;
				consumeOrError(TokenType.SEMICOLON, "Expected semi colon in for loop");

				Expr increment = !check(TokenType.SEMICOLON) ? expression() : null;

				consumeOrError(TokenType.RIGHT_PAREN, "Expected a ')' at the end of a for statement");

				Stmt body = statement();
				
				if(increment != null) {
						body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
				}

				if(condition == null) condition = new Expr.Literal(true);
				body = new Stmt.While(condition, body);

				if(initializer != null) body = new Stmt.Block(Arrays.asList(initializer, body));

				return body;
		}

		private Stmt whileStatement() {
				consumeOrError(TokenType.LEFT_PAREN, "excepted '(' at the beginning of the while loop");
				Expr condition = expression();
				consumeOrError(TokenType.RIGHT_PAREN, "expected ')' at the end of the while condition");
				Stmt body = statement();
				return new Stmt.While(condition, body);
		}

		private Stmt ifStatement() {
				consumeOrError(TokenType.LEFT_PAREN, "Missing '(");
				Expr condition = expression();
				consumeOrError(TokenType.RIGHT_PAREN, "Missing ')' in if statement");
				Stmt thenBranch = statement();
				Stmt elseBranch = matchAny(TokenType.ELSE) ? statement() : null;

				return new Stmt.If(condition, thenBranch, elseBranch);
		}

		private Stmt block() {
				return new Stmt.Block(getBlockStatements());
		}

		private List<Stmt> getBlockStatements() {
				List<Stmt> ret = new ArrayList<Stmt>();
				while(!check(TokenType.RIGHT_BRACE) && !reachedEOF()) {
						ret.add(declaration());
				}
				consumeOrError(TokenType.RIGHT_BRACE, "Expected } after block creation");
				return ret;
		}

		private Stmt printStatement() {
				Expr expr = expression();
				consumeOrError(TokenType.SEMICOLON, "Expected ';' after print stmt");
				return new Stmt.Print(expr);
		}

		private Stmt expressionStatement() {
				Expr expr = expression();
				consumeOrError(TokenType.SEMICOLON, "Expected ';' after print stmt");
				return new Stmt.Expression(expr);
		}

		private Expr expression() {
				return assignement();
		}

		private Expr assignement() {
				Expr left = or();

				if(matchAny(TokenType.EQUAL)) {
						Expr right = assignement();

						if(left instanceof Expr.Variable) {
								Token name = ((Expr.Variable) left).name;
								return new Expr.Assign(name, right);
						}
						if(left instanceof Expr.Get) {
								Expr.Get get = (Expr.Get) left;
								return new Expr.Set(get.object, get.name, right);
						}

						throw new RuntimeException("Invalid assignement target");
				}
				return left;
		}
		
		private Expr or() {
				Expr left = and();

				if(matchAny(TokenType.OR)) {
						Token operator = previous();
						Expr right = assignement();
						left = new Expr.Logical(left, operator, right);
				}
				return left;
		}

		private Expr and() {
				Expr left = equality();
				if(matchAny(TokenType.AND)) {
						Token operator = previous();
						Expr right = assignement();
						left = new Expr.Logical(left, operator, right);
				}
				return left;
		}

		private Expr equality() {
				Expr left = comparison();
				while(matchAny(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
						Token operator = previous();
						Expr right = comparison();
						left = new Expr.Binary(left, operator, right);
				}
				return left;
		}
		
		private Expr comparison() {
				Expr left = term();
				while(matchAny(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
						Token operator = previous();
						Expr right = term();
						left = new Expr.Binary(left, operator, right);
				}
				return left;
		}

		private Expr term() {
				Expr left = factor();
				while(matchAny(TokenType.MINUS, TokenType.PLUS)) {
						Token operator = previous();
						Expr right = factor();
						left = new Expr.Binary(left, operator, right);
				}
				return left;
		}

		private Expr factor() {
				Expr left = unary();
				while(matchAny(TokenType.SLASH, TokenType.STAR)) {
						Token operator = previous();
						Expr right = unary();
						left = new Expr.Binary(left, operator, right);
				}
				return left;
		}

		private Expr unary() {
				if(matchAny(TokenType.BANG, TokenType.MINUS)) {
						Token operator = previous();
						return new Expr.Unary(operator, unary());
				}
				return call();
		}

		private Expr call() {
				Expr callee = primary();
				while(true) {
						if(matchAny(TokenType.LEFT_PAREN)) {
								callee = parseArguments(callee);
						}
						else if(matchAny(TokenType.DOT)) {
								Token name = consumeOrError(TokenType.IDENTIFIER, "Expected property name after .");
								callee = new Expr.Get(callee, name);
						}
						else {
								break;
						}
				}
				return callee;
		}

		private Expr parseArguments(Expr callee) {
				List<Expr> arguments = new ArrayList<Expr>();

				if(!check(TokenType.RIGHT_PAREN)) {
						do {
								arguments.add(expression());
						} while(matchAny(TokenType.COMMA));
				}

				Token paren = consumeOrError(TokenType.RIGHT_PAREN, "expected a '(' at the end of a function call");

				if(arguments.size() >= 255) error(peek(), "Too many arguments (> 255)");

				return new Expr.Call(callee, paren, arguments);
		}

		private Expr primary() {
				if(matchAny(TokenType.FALSE)) return new Expr.Literal(false);
				if(matchAny(TokenType.TRUE)) return new Expr.Literal(true);
				if(matchAny(TokenType.NIL)) return new Expr.Literal(null);

				if(matchAny(TokenType.NUMBER, TokenType.STRING)) {
						return new Expr.Literal(previous().literal);
				}

				if(matchAny(TokenType.THIS)) return new Expr.This(previous());

				if(matchAny(TokenType.LEFT_PAREN)) {
						Expr expr = expression();
						consumeOrError(TokenType.RIGHT_PAREN, "Expected a ')' after expression.");
						return new Expr.Grouping(expr);
				}

				if(matchAny(TokenType.SUPER)) {
						Token keyword = previous();
						consumeOrError(TokenType.DOT, "Expected a '.' after keyword 'super'");
						Token method = consumeOrError(TokenType.IDENTIFIER, "Expected a method name after super.");
						return new Expr.Super(keyword, method);
				}

				if(matchAny(TokenType.IDENTIFIER)) {
						return new Expr.Variable(previous());
				}

				System.out.println("Parser.java " + peek());
				throw	error(peek(), "Expected expression");
		}

		private Token consumeOrError(TokenType type, String message) {
				if(check(type)) return getToken();
				throw error(peek(), message);
		}

		private RuntimeException error(Token token, String message) {
				Lox.error(token.line, message);
				return new RuntimeException(message);
		}

		private boolean matchAny(TokenType... types) {
				for(TokenType type : types) {
						if(!check(type)) continue;
						getToken();
						return true;
				}
				return false;
		}

		private boolean check(TokenType type) {
				if(reachedEOF()) return false;
				return peek().type == type;
		}

		private Token getToken() {
				current += reachedEOF() ? 0 : 1;
				return previous();
		}
		
		private Token previous() {
				return tokens.get(current - 1);
		}
		
		private Token peek() {
				if(reachedEOF()) return previous();
				return tokens.get(current);
		}

		private boolean reachedEOF() {
				return current >= tokens.size() - 1;
		}
}
