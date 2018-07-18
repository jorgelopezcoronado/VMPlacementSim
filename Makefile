jar: compile
	jar cf VMPS.jar vmplacementsim/*.class	
compile:
	javac vmplacementsim/*.java

clean:
	rm vmplacementsim/*.class
	rm *.jar

redo: clean compile

commit: 
	echo "Enter message for commit: "
	@-read commitm; \
	git commit -m "$$commitm"
	git push origin master 
