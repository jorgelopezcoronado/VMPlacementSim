all:
	javac *.java

clean:
	rm *.class

redo: clean all

commit: 
	echo "Enter message for commit: "
	@-read commitm; \
	git commit -m "$$commitm"
	git push origin master 
