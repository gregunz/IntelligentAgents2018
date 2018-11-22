mkdir -p tournament
mkdir -p tournament/$1
rm tournament/$1/agents.xml
cp config/agents.xml tournament/$1/agents.xml
java -jar ../logist/logist.jar -run $1 config/$2.xml
java -jar ../logist/logist.jar -score $1