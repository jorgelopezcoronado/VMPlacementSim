jar: compile
	jar cf VMPS.jar *.class	
compile:
	javac *.java

clean:
	rm *.class
	rm *.jar

redo: clean compile

commit: 
	echo "Enter message for commit: "
	@-read commitm; \
	git commit -m "$$commitm"
	git push origin master 
