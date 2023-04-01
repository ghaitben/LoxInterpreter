import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public class Tokenizer {
		private final String source;
		private final List<Token> tokens;
		private static final Map<String, TokenType> keywords;
		static {
				keywords = new HashMap<>();
				keywords.put("and",   TokenType.AND);
				keywords.put("class", TokenType.CLASS);
				keywords.put("else",  TokenType.ELSE);
				keywords.put("false", TokenType.FALSE);
				keywords.put("for",   TokenType.FOR);
				keywords.put("fun",   TokenType.FUN);
				keywords.put("if",    TokenType.IF);
				keywords.put("nil",   TokenType.NIL);
				keywords.put("or",    TokenType.OR);
				keywords.put("print", TokenType.PRINT);
				keywords.put("return",TokenType.RETURN);
				keywords.put("super", TokenType.SUPER);
				keywords.put("this",  TokenType.THIS);
				keywords.put("true",  TokenType.TRUE);
				keywords.put("var",   TokenType.VAR);
				keywords.put("while", TokenType.WHILE);
				keywords.put("max", TokenType.MAX);
				keywords.put("break", TokenType.BREAK);
		}
		private int start = 0;
		private int current = 0;
		private int line = 1;

		Tokenizer(String source) {
				this.source = source;
				this.tokens = new ArrayList<Token>();
		}

		List<Token> scanTokens() {
				while(!reachedEOF()) { scanToken(); }
				tokens.add(new Token(TokenType.EOF, "", null, line));
				return tokens;
		}

		private void scanToken() {
				start = current;
				char c = getChar();
				switch(c) {
						case '(':
								addToken(TokenType.LEFT_PAREN, null);
								break;
						case ')':
								addToken(TokenType.RIGHT_PAREN, null);
								break;
						case '{':
								addToken(TokenType.LEFT_BRACE, null);
								break;
						case '}':
								addToken(TokenType.RIGHT_BRACE, null);
								break;
						case ',':
								addToken(TokenType.COMMA, null);
								break;
						case '.':
								addToken(TokenType.DOT, null);
								break;
						case '-':
								addToken(TokenType.MINUS, null);
								break;
						case '+':
								addToken(TokenType.PLUS, null);
								break;
						case '*':
								addToken(TokenType.STAR, null);
								break;
						case ';':
								addToken(TokenType.SEMICOLON, null);
								break;
						case '?':
								addToken(TokenType.QUESTION_MARK, null);
								break;
						case ':':
								addToken(TokenType.COLON, null);
								break;
						case '!':
								addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG, null);
								break;
						case '<':
								addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS, null);
								break;
						case '>':
								addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER, null);
								break;
						case '=':
								addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL, null);
								break;
						case '/':
								if(match('/')) comment();
								else if(match('*')) multilineComment();
								else addToken(TokenType.SLASH, null);
								break;
						case ' ':
								break;
						case '\r':
								break;
						case '\t':
								break;
						case '\n':
								line++;
								break;
						case '"':
								string();
								break;
						default:
								if(isDigit(c)) { number(); }
								else if(isAlpha(c)) { identifier(); }
								else { Lox.error(line, "Unexpected character"); }
								break;
				}
		}

		private void multilineComment() {
				while(!reachedEOF() && !(peek() == '*' && peekNext() == '/')) {
						if(getChar() == '\n') line++;
				}
				if(reachedEOF()){ Lox.error(line, "Missing closing comment symbol */"); }
				else { getChar(); getChar(); }
		}

		private void identifier() {
				while(isAlphaNumeric(peek())) getChar();
				String text = source.substring(start, current);
				TokenType type = keywords.get(text);
				addToken(type == null ? TokenType.IDENTIFIER : type, null);
		}

		private boolean isAlpha(char c) {
				return (c >= 'a' && c <= 'z') ||
						   (c >= 'A' && c <= 'Z') ||
						   c == '_';
		}

		private boolean isAlphaNumeric(char c) {
				return isAlpha(c) || isDigit(c);
		}

		private void number() {
				while(isDigit(peek())) getChar();
				if(peek() == '.' && isDigit(peekNext())) getChar();
				while(isDigit(peek())) getChar();
				String literal_value = source.substring(start, current);
				addToken(TokenType.NUMBER, Double.parseDouble(literal_value));
		}

		private boolean isDigit(char c) {
				return c >= '0' && c <= '9';
		}

		private void string() {
				while(peek() != '"' && !reachedEOF()) {
						if(peek() == '\n') line++;
						getChar();
				}
				if(reachedEOF()) {
						Lox.error(line, "Unterminated string");
						return;
				}
				getChar();
				String literal_value = source.substring(start + 1, current - 1);
				addToken(TokenType.STRING, literal_value);
		}

		private void comment() {
				while(peek() != '\n' && !reachedEOF()) getChar();
		}

		private char getChar() {
				return source.charAt(current++);
		}

		private boolean reachedEOF() {
				return current >= source.length();
		}

		private char peek() {
				if(reachedEOF()) return '\0';
				return source.charAt(current);
		}

		private char peekNext() {
				if(current + 1 >= source.length()) return '\0';
				return source.charAt(current + 1);
		}

		private boolean match(char c) {
				if(reachedEOF() || source.charAt(current) != c) return false;
				current++;
				return true;
		}

		private void addToken(TokenType type, Object literal) {
				String text = source.substring(start, current);
				tokens.add(new Token(type, text, literal, line));
		}
}
