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

Esta web está implementada con ```spring-session```, que mantiene los datos de la sesión en un servicio compartido externo, de forma que varias réplicas pueden acceder a los datos de la sesión. En concreto, en este ejemplo se usa MySQL como sitio centralizado para guardar los datos de la sesión de los usuarios.
 