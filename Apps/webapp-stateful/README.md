# Testing

Esta aplicación asigna un color aleatorio a cada usuario que se conecta. En las sucesivas peticiones a la web, se genera la página con el color asignado inicialmente.

Para hacer esto, hace uso de la sesión HttpSession proporcionada por el framework Java EE.

Para construir la aplicación y publicarla en DockerHub se usa el plugin maven JIB con el comando:

`
$ mvn package
`

Para desplegar la aplicación en Kubernetes se usa el comando

`
$ kubectl create -f kubernetes\webapp.yml
`

En minikube, se puede abrir el browser directamente apuntando al servicio:

`
$ minikube service webapp
`

Para probar el funcionamiento, basta con abrir dos navegadores web y verificar que cada uno de ellos tiene un color diferente.

## Escalado

Para escalar la aplicación basta con pedir a Kubernetes más réplicas del pod:

`
$ kubectl scale deployments/webapp --replicas=2
`

En este caso se puede ver cómo diferentes peticiones del mismo usuario son atendidas por diferentes pods. Pero como la aplicación no está implementada correctamente, cada vez que hay un cambio de pod, la webapp considera que el usuario es nuevo y por tanto le da un color diferente. 

Esto es debido a cómo se implementa la gestión de sesiones de usuario. Cada vez que se conecta un nuevo usuario a la web, se genera un token y se envía ese token al browser para que lo envíe en las siguientes peticiones. El problema es que cada pod es independiente, y cuando una petición desde el browser lleva un token que no ha sido generado por ese pod, se asume que el usuario es nuevo y se genera un nuevo token. Por este motivo, cada vez que el browser cambia de pod, cambia el color, porque el pod que recibe la petición lo considera un nuevo usuario.

Para solucionar el problema, es necesario utilizar técnicas para que la información de la sesión sea compartida entre varios pods.
 