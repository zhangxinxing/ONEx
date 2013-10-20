lib='.'
for i in `find lib -name *.jar`
do
	lib=$lib:$i;
done;
javac -g -cp $lib -d ./build `find src -name *.java`;
