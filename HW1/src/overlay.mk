SRC = $(find -type f -name \*.java)

.java: .class
	javac $*.java

default: classes

classes: $(SRC:.java=.class)

clean:
	$(RM) *.class