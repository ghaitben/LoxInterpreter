import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
		private static boolean errorOccured = false;
		private static boolean runtimeErrorOccured = false;
		private static Interpreter interpreter = new Interpreter();

		public static void main(String[] args) throws IOException {
				if(args.length > 1) {
						System.out.println("Usage: jlox [script]");
						System.exit(64); 
				}
				if(args.length == 1) {
						runFile(args[0]);
				}
				else {
						runPrompt();
				}
		}

		private static void runFile(String path) throws IOException {
				byte[] bytes = Files.readAllBytes(Paths.get(path));
				run(new String(bytes, Charset.defaultCharset()));
				if(errorOccured) System.exit(65);
				if(runtimeErrorOccured) System.exit(76);
		}

		private static void runPrompt() throws IOException {
				InputStreamReader input = new InputStreamReader(System.in);
				BufferedReader reader = new BufferedReader(input);
				while(true) {
						System.out.print(">>> ");
						String line;
						if((line = reader.readLine()) != null) run(line);
						else break;
						errorOccured = false;
						runtimeErrorOccured = false;
				}
		}

		private static void run(String source) {
				Tokenizer tokenizer = new Tokenizer(source);
				List<Token> tokens = tokenizer.scanTokens();

				Parser parser = new Parser(tokens);
				List<Stmt> ss = parser.parse();

				if(errorOccured) return;

				Resolver resolver = new Resolver(interpreter);
				for(Stmt s : ss) s.accept(resolver);

				interpreter.interpret(ss);
		}

		public static void runtimeError(RuntimeException e) {
				System.err.println(e.getMessage());
				runtimeErrorOccured = true;
		}

		public static void error(int line, String message) {
				report(line, "", message);
		}

		private static void report(int line, String where, String message) {
				System.err.println("[line " + line + "] Error" + where + ": " + message);
				errorOccured = true;
		}

}
