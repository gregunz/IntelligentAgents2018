mkdir tournament
java -jar ../logist/logist.jar -new $1 config/agents.xml
rm tournament/$1/agents.xml
cp config/agents.xml tournament/$1/agents.xml
java -jar ../logist/logist.jar -run $1 config/auction.xml
java -jar ../logist/logist.jar -run $1 config/auction2.xml
java -jar ../logist/logist.jar -run $1 config/auction3.xml
java -jar ../logist/logist.jar -score $1 $2