APP_NAME=btc-collider

port=7000

#To enable JMX port
IP=$(hostname -I | awk '{print $1}')

# JMX Port is app's base port + 10001
PORT=$((${port}+10001))
JAVA_OPTS="--add-exports java.base/jdk.internal.ref=ALL-UNNAMED
  --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED
  --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED
  --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED -Dcom.sun.management.jmxremote
  -Dcom.sun.management.jmxremote.port=$PORT -Dcom.sun.management.jmxremote.authenticate=false
 -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=$IP -Xmx512m -Djava.awt.headless=true
 -XX:+UnlockExperimentalVMOptions -XX:+UseZGC"

nice --adjustment=10 java ${JAVA_OPTS} -jar btc-collider-0.0.1.jar