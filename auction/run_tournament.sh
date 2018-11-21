mkdir -p tournament
java -jar ../logist/logist.jar -new $1 config/agents.xml
rm tournament/$1/agents.xml
cp config/agents.xml tournament/$1/agents.xml
java -jar ../logist/logist.jar -run $1 config/auction15.xml
java -jar ../logist/logist.jar -run $1 config/auction20.xml
java -jar ../logist/logist.jar -run $1 config/auction30.xml
java -jar ../logist/logist.jar -run $1 config/auction50.xml
java -jar ../logist/logist.jar -score $1 $2