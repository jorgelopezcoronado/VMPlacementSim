all:
	javac *.java

clean:
	rm *.class

redo: clean all

commit: clean
	echo -n "Enter message for commit: "
	read commitm
	git commit -m $commitm
	git push origin master 
