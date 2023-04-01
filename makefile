compile:
	@javac *.java; rm *.class

run:
	@javac *.java && java Lox; rm *.class

run_with_file:
	@javac *.java && java Lox data/lox_script.txt; rm *.class
