import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
		public static void main(String[] args) throws IOException {
				String output_dir = "";

				defineAst(output_dir, "Expr", Arrays.asList(
					"Binary   : Expr left, Token operator, Expr right",
					"Grouping : Expr expression",
					"Literal  : Object value",
					"Unary    : Token operator, Expr right",
					"Variable : Token name",
					"Assign   : Token name, Expr value",
					"Logical  : Expr left, Token operator, Expr right",
					"Call     : Expr callee, Token paren, List<Expr> arguments",
					"Get      : Expr object, Token name",
					"Set      : Expr object, Token name, Expr value",
					"This     : Token keyword",
					"Super    : Token keyword, Token method"
				));

				defineAst(output_dir, "Stmt", Arrays.asList(
					"Expression : Expr expression",
					"If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
					"While      : Expr condition, Stmt body",
					"Print      : Expr expression",
					"Var        : Token name, Expr initializer",
					"Block      : List<Stmt> statements",
					"Break      : Expr condition",
					"Function   : Token name, List<Token> arguments, List<Stmt> body",
					"Return     : Token keyword, Expr value",
					"Class      : Token name, List<Stmt.Function> methods, Expr.Variable superclass"
				));
		}

		private static void defineAst(String output_dir, String baseName, List<String> types) throws IOException {
				PrintWriter writer = new PrintWriter(output_dir + baseName + ".java", "UTF-8");

				writer.println("import java.util.List;");
				writer.println();
				writer.println("abstract class " + baseName + " {");
				writer.println(tab(1) + "abstract <T> T accept(Visitor<T> visitor);");
				writer.println();
				//define the visitor interface
				defineVisitor(writer, baseName, types);
				//define subclasses
				for(String classType : types) {
						String className = classType.split(":")[0].trim();
						String fields = classType.split(":")[1].trim();
						defineType(writer, baseName, className, fields);
				}
				writer.println("}");
				writer.close();
		}

		private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
				writer.println(tab(1) + "interface Visitor<T> {");
				for(String type : types) {
						String typeName = type.split(":")[0].trim();
						writer.println(tab(2) + "T visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
				}
				writer.println(tab(1) + "}");
				writer.println();
		}

		private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
				writer.println(tab(1) + "public static class " + className + " extends " + baseName + "{");	
				String[] fieldList = fields.split(",");
				//fields
				for(String field : fieldList) {
						String type = field.trim().split(" ")[0];
						String name = field.trim().split(" ")[1];
						writer.println(tab(2) + type + " " + name + ";");
				}
				writer.println();
				//Constructor
				writer.println(tab(2) + className + "(" + fields + ")" + " {");
				//initialization
				for(String field : fieldList) {
						String name = field.trim().split(" ")[1];
						writer.println(tab(3) + "this." + name + " = " + name + ";");
				}
				writer.println(tab(2) + "}");
				writer.println();
				//The visitor's accept method
				writer.println(tab(2) + "@Override");
				writer.println(tab(2) + "<T> T accept(Visitor<T> visitor) {");
				writer.println(tab(3) + "return visitor.visit" + className + baseName + "(this);");
				writer.println(tab(2) + "}");
				writer.println(tab(1) + "}");
				writer.println();
		}
		private static String tab(int tabs) {
				return new String(new char[tabs << 1]).replace("\0", "\t");
		}
}
